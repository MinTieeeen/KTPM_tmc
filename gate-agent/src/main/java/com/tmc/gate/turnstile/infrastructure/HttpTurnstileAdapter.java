package com.tmc.gate.turnstile.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tmc.gate.access.domain.CommandStatus;
import com.tmc.gate.access.domain.GateResult;
import com.tmc.gate.access.domain.OpenCommand;
import com.tmc.gate.access.domain.TurnstileGateway;
import com.tmc.gate.access.domain.TurnstileHealth;
import com.tmc.gate.access.domain.TurnstileHealthReader;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Adapter HTTP tới hệ thống cổng xoay (hoặc {@code turnstile-sim}) – mục 6.4 spec gate-agent-turnstile.
 * <ul>
 *   <li>Timeout theo <b>từng lời gọi</b> bằng tham số của port ({@code HttpRequest.timeout} + {@code orTimeout} làm
 *       chốt chặn cứng), connect timeout riêng ≤ 300 ms (NFR-02) – D1.</li>
 *   <li>Một circuit breaker Resilience4j cho mỗi cổng ({@code turnstile-<id>}, cấu hình gốc {@code turnstile}) – D2;
 *       4xx không tính là lỗi của breaker – D3.</li>
 *   <li>{@code status()} không đi qua breaker để vẫn xác minh được lệnh khi mạch vừa mở.</li>
 * </ul>
 * Không ngoại lệ nào thoát khỏi adapter (LSP); an toàn đa luồng.
 */
public class HttpTurnstileAdapter implements TurnstileGateway, TurnstileHealthReader {

    private static final Logger log = LoggerFactory.getLogger(HttpTurnstileAdapter.class);
    private static final String BREAKER_CONFIG = "turnstile";
    private static final String BREAKER_PREFIX = "turnstile-";

    private final HttpClient http;
    private final URI baseUrl;
    private final CircuitBreakerRegistry breakers;
    private final ObjectMapper objectMapper;
    private final Map<String, TurnstileHealth> lastHealth = new ConcurrentHashMap<>();
    private final Map<String, CircuitBreaker> breakerById = new ConcurrentHashMap<>();

    public HttpTurnstileAdapter(TurnstileProperties props, CircuitBreakerRegistry breakers, ObjectMapper objectMapper) {
        this.http = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(props.connectTimeout())
                .build();
        String url = props.url().toString();
        this.baseUrl = URI.create(url.endsWith("/") ? url : url + "/");
        this.breakers = breakers;
        this.objectMapper = objectMapper;
    }

    @Override
    public GateResult open(OpenCommand cmd, Duration timeout) {
        String id = cmd.turnstileId();
        CircuitBreaker cb = breaker(id);
        if (!cb.tryAcquirePermission()) {
            lastHealth.put(id, TurnstileHealth.CIRCUIT_OPEN);
            log.warn("Cổng {}: mạch đang mở, không gửi lệnh {}", id, cmd.requestId());
            return GateResult.CIRCUIT_OPEN;
        }

        long start = System.nanoTime();
        CompletableFuture<HttpResponse<Void>> future = null;
        try {
            HttpRequest request = HttpRequest.newBuilder(baseUrl.resolve("turnstiles/" + id + "/open"))
                    .timeout(timeout)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofByteArray(
                            objectMapper.writeValueAsBytes(new OpenRequestBody(cmd.requestId()))))
                    .build();
            future = http.sendAsync(request, HttpResponse.BodyHandlers.discarding());
            HttpResponse<Void> response = future.orTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS).join();
            int code = response.statusCode();
            long elapsed = System.nanoTime() - start;

            if (code >= 200 && code < 300) {
                cb.onSuccess(elapsed, TimeUnit.NANOSECONDS);
                lastHealth.put(id, TurnstileHealth.UP);
                return GateResult.OPENED;
            }
            if (code >= 400 && code < 500) {
                cb.releasePermission(); // lỗi của lệnh, không phản ánh tình trạng cổng (D3)
                log.warn("Cổng {}: lệnh {} bị từ chối HTTP {}", id, cmd.requestId(), code);
                return GateResult.ERROR;
            }
            cb.onError(elapsed, TimeUnit.NANOSECONDS, new IOException("HTTP " + code));
            lastHealth.put(id, TurnstileHealth.DOWN);
            log.warn("Cổng {}: lệnh {} lỗi HTTP {}", id, cmd.requestId(), code);
            return GateResult.ERROR;
        } catch (CompletionException e) {
            return failed(cb, id, cmd.requestId(), start, e.getCause() != null ? e.getCause() : e, future);
        } catch (Exception e) {
            return failed(cb, id, cmd.requestId(), start, e, future);
        }
    }

    private GateResult failed(CircuitBreaker cb, String id, UUID requestId, long start, Throwable cause,
                              CompletableFuture<?> future) {
        if (future != null) {
            future.cancel(true);
        }
        cb.onError(System.nanoTime() - start, TimeUnit.NANOSECONDS, cause);
        lastHealth.put(id, TurnstileHealth.DOWN);
        boolean timedOut = cause instanceof HttpTimeoutException || cause instanceof TimeoutException;
        log.warn("Cổng {}: lệnh {} {} ({})", id, requestId, timedOut ? "TIMEOUT" : "ERROR",
                cause.getClass().getSimpleName());
        return timedOut ? GateResult.TIMEOUT : GateResult.ERROR;
    }

    @Override
    public CommandStatus status(String turnstileId, UUID requestId, Duration timeout) {
        try {
            HttpRequest request = HttpRequest.newBuilder(
                            baseUrl.resolve("turnstiles/" + turnstileId + "/commands/" + requestId))
                    .timeout(timeout)
                    .GET()
                    .build();
            HttpResponse<byte[]> response = http.sendAsync(request, HttpResponse.BodyHandlers.ofByteArray())
                    .orTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS)
                    .join();
            if (response.statusCode() != 200) {
                return CommandStatus.UNKNOWN;
            }
            CommandStatusBody body = objectMapper.readValue(response.body(), CommandStatusBody.class);
            return body.executed() ? CommandStatus.EXECUTED : CommandStatus.NOT_EXECUTED;
        } catch (Exception e) {
            log.warn("Cổng {}: không kiểm tra được lệnh {} ({})", turnstileId, requestId, e.getClass().getSimpleName());
            return CommandStatus.UNKNOWN;
        }
    }

    @Override
    public TurnstileHealth health(String turnstileId) {
        CircuitBreaker cb = breakerById.get(turnstileId);
        if (cb != null && (cb.getState() == CircuitBreaker.State.OPEN
                || cb.getState() == CircuitBreaker.State.FORCED_OPEN)) {
            return TurnstileHealth.CIRCUIT_OPEN;
        }
        return lastHealth.getOrDefault(turnstileId, TurnstileHealth.UP);
    }

    private CircuitBreaker breaker(String turnstileId) {
        return breakerById.computeIfAbsent(turnstileId, id -> {
            CircuitBreaker cb = breakers.circuitBreaker(BREAKER_PREFIX + id, BREAKER_CONFIG);
            cb.getEventPublisher().onStateTransition(e ->
                    log.warn("Cổng {}: circuit breaker {}", id, e.getStateTransition()));
            return cb;
        });
    }
}
