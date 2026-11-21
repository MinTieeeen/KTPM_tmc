package com.tmc.sim.turnstile.application;

import com.tmc.sim.turnstile.domain.Behavior;
import com.tmc.sim.turnstile.domain.BehaviorMode;
import com.tmc.sim.turnstile.domain.SimulatedFailureException;
import com.tmc.sim.turnstile.domain.Sleeper;
import com.tmc.sim.turnstile.domain.Turnstile;
import com.tmc.sim.turnstile.domain.TurnstileRegistry;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.random.RandomGenerator;

/** Áp hành vi hiện tại cho mỗi lệnh mở; tra lệnh; trạng thái cổng (mục 6.4). */
@Service
public class TurnstileSimulatorService {

    private final TurnstileRegistry registry;
    private final BehaviorHolder behavior;
    private final Sleeper sleeper;
    private final RandomGenerator random;
    private final Clock clock;
    private final Duration timeoutDelay;

    public TurnstileSimulatorService(TurnstileRegistry registry, BehaviorHolder behavior, Sleeper sleeper,
                                     RandomGenerator random, Clock clock, SimProperties props) {
        this.registry = registry;
        this.behavior = behavior;
        this.sleeper = sleeper;
        this.random = random;
        this.clock = clock;
        this.timeoutDelay = props.timeoutDelay();
    }

    /**
     * @throws SimulatedFailureException theo chế độ hiện tại (ERROR → 500, TIMEOUT → 504)
     */
    public OpenResult open(String turnstileId, UUID requestId) {
        Turnstile t = registry.getOrCreate(turnstileId);
        Optional<Instant> previous = t.executedAt(requestId);
        if (previous.isPresent()) {
            // Idempotent: trả kết quả cũ, bỏ qua chế độ hiện tại (D1)
            return new OpenResult(requestId, true, true, previous.get());
        }

        Behavior b = behavior.get();
        switch (b.mode()) {
            case ERROR -> throw new SimulatedFailureException(SimulatedFailureException.Kind.ERROR,
                    "Cổng " + turnstileId + " đang lỗi (giả lập)");
            case TIMEOUT -> {
                sleeper.sleep(timeoutDelay);
                throw new SimulatedFailureException(SimulatedFailureException.Kind.TIMEOUT,
                        "Cổng " + turnstileId + " không phản hồi (giả lập)");
            }
            case EXECUTE_THEN_TIMEOUT -> {
                boolean first = t.execute(requestId, clock.instant());
                sleeper.sleep(timeoutDelay);
                return new OpenResult(requestId, true, !first, t.executedAt(requestId).orElseThrow());
            }
            case NORMAL -> {
                sleeper.sleep(Duration.ofMillis(randomLatency(b)));
                if (b.failureRate() > 0 && random.nextDouble() < b.failureRate()) {
                    throw new SimulatedFailureException(SimulatedFailureException.Kind.ERROR,
                            "Lỗi ngẫu nhiên của cổng " + turnstileId + " (giả lập)");
                }
                boolean first = t.execute(requestId, clock.instant());
                return new OpenResult(requestId, true, !first, t.executedAt(requestId).orElseThrow());
            }
        }
        throw new IllegalStateException("Chế độ không xử lý: " + b.mode());
    }

    /** Tra lệnh ngay, không chịu độ trễ hay chế độ (FR-06, D2). Rỗng nếu chưa thực hiện. */
    public Optional<Instant> commandExecutedAt(String turnstileId, UUID requestId) {
        return registry.find(turnstileId).flatMap(t -> t.executedAt(requestId));
    }

    public Turnstile status(String turnstileId) {
        return registry.getOrCreate(turnstileId);
    }

    public List<Turnstile> all() {
        return registry.all();
    }

    /** {@code DOWN} khi chế độ ERROR, ngược lại {@code UP} (FR-07). */
    public String health() {
        return behavior.get().mode() == BehaviorMode.ERROR ? "DOWN" : "UP";
    }

    private long randomLatency(Behavior b) {
        if (b.latencyMaxMs() == b.latencyMinMs()) {
            return b.latencyMinMs();
        }
        return random.nextLong(b.latencyMinMs(), b.latencyMaxMs() + 1);
    }
}
