package com.tmc.sim.turnstile.infrastructure;

import com.tmc.sim.turnstile.domain.Sleeper;
import org.springframework.stereotype.Component;

import java.time.Duration;

/** Chờ thật bằng {@code Thread.sleep} (chạy trên luồng ảo nên không chiếm luồng nền tảng). */
@Component
public class ThreadSleeper implements Sleeper {

    @Override
    public void sleep(Duration duration) {
        if (duration.isZero() || duration.isNegative()) {
            return;
        }
        try {
            Thread.sleep(duration);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
