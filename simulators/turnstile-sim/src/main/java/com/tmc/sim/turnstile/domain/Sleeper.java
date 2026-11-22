package com.tmc.sim.turnstile.domain;

import java.time.Duration;

/** Cách chờ – tiêm được để unit test không phải chờ thật (D4, NFR-03). */
public interface Sleeper {

    void sleep(Duration duration);
}
