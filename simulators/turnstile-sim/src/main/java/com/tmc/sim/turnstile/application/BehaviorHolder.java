package com.tmc.sim.turnstile.application;

import com.tmc.sim.turnstile.domain.Behavior;
import com.tmc.sim.turnstile.domain.BehaviorMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;

/** Giữ hành vi hiện tại; cập nhật từng phần, có kiểm tra, có hiệu lực ngay với yêu cầu tiếp theo (FR-08). */
@Component
public class BehaviorHolder {

    private static final Logger log = LoggerFactory.getLogger(BehaviorHolder.class);

    private final AtomicReference<Behavior> current;

    public BehaviorHolder(SimProperties props) {
        this.current = new AtomicReference<>(new Behavior(props.latencyMin().toMillis(), props.latencyMax().toMillis(),
                props.failureRate(), props.mode()));
    }

    public Behavior get() {
        return current.get();
    }

    /**
     * Cập nhật từng phần: tham số {@code null} giữ giá trị cũ.
     *
     * @throws IllegalArgumentException dữ liệu không hợp lệ (min > max, failureRate ngoài [0,1], mode lạ)
     */
    public Behavior update(Long latencyMinMs, Long latencyMaxMs, Double failureRate, String mode) {
        Behavior updated = current.updateAndGet(old -> new Behavior(
                latencyMinMs != null ? latencyMinMs : old.latencyMinMs(),
                latencyMaxMs != null ? latencyMaxMs : old.latencyMaxMs(),
                failureRate != null ? failureRate : old.failureRate(),
                mode != null ? parseMode(mode) : old.mode()));
        log.info("Hành vi mới: {}", updated);
        return updated;
    }

    private static BehaviorMode parseMode(String mode) {
        try {
            return BehaviorMode.valueOf(mode.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("mode không hợp lệ: " + mode);
        }
    }
}
