package com.tmc.gate.turnstile.infrastructure;

import com.tmc.gate.access.domain.CommandStatus;
import com.tmc.gate.access.domain.GateResult;
import com.tmc.gate.access.domain.OpenCommand;
import com.tmc.gate.access.domain.TurnstileGateway;
import com.tmc.gate.access.domain.TurnstileHealth;
import com.tmc.gate.access.domain.TurnstileHealthReader;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cổng xoay giả lập trong bộ nhớ, không I/O – thay thế cho {@code HttpTurnstileAdapter} ở profile {@code fake}
 * và trong test (LSP: cùng hợp đồng, luôn trả trong {@code timeout}, không ném ngoại lệ).
 * Hành vi và độ trễ đổi được khi đang chạy. An toàn đa luồng.
 */
public class FakeTurnstileGateway implements TurnstileGateway, TurnstileHealthReader {

    private volatile FakeBehavior behavior;
    private volatile Duration latency;
    private final Set<UUID> executed = ConcurrentHashMap.newKeySet();
    private final Map<String, TurnstileHealth> lastHealth = new ConcurrentHashMap<>();

    public FakeTurnstileGateway(FakeBehavior behavior, Duration latency) {
        this.behavior = Objects.requireNonNull(behavior, "behavior");
        this.latency = Objects.requireNonNull(latency, "latency");
    }

    public void setBehavior(FakeBehavior behavior) {
        this.behavior = Objects.requireNonNull(behavior, "behavior");
    }

    public void setLatency(Duration latency) {
        this.latency = Objects.requireNonNull(latency, "latency");
    }

    public FakeBehavior behavior() {
        return behavior;
    }

    @Override
    public GateResult open(OpenCommand cmd, Duration timeout) {
        GateResult result = switch (behavior) {
            case OPEN -> {
                if (latency.compareTo(timeout) > 0) {
                    // Trễ hơn ngân sách: vẫn phải trả trong timeout (LSP) và coi như cổng chưa mở.
                    sleep(timeout);
                    yield GateResult.TIMEOUT;
                }
                sleep(latency);
                executed.add(cmd.requestId());
                yield GateResult.OPENED;
            }
            case TIMEOUT -> {
                sleep(timeout);
                yield GateResult.TIMEOUT;
            }
            case ERROR -> GateResult.ERROR;
            case EXECUTE_THEN_TIMEOUT -> {
                executed.add(cmd.requestId());
                sleep(timeout);
                yield GateResult.TIMEOUT;
            }
        };
        lastHealth.put(cmd.turnstileId(), result == GateResult.OPENED ? TurnstileHealth.UP : TurnstileHealth.DOWN);
        return result;
    }

    @Override
    public CommandStatus status(String turnstileId, UUID requestId, Duration timeout) {
        return executed.contains(requestId) ? CommandStatus.EXECUTED : CommandStatus.NOT_EXECUTED;
    }

    @Override
    public TurnstileHealth health(String turnstileId) {
        return lastHealth.getOrDefault(turnstileId, TurnstileHealth.UP);
    }

    private static void sleep(Duration d) {
        if (d.isZero() || d.isNegative()) {
            return;
        }
        try {
            Thread.sleep(d);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
