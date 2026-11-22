package com.tmc.sim.turnstile.api;

import com.tmc.sim.turnstile.api.SimDtos.BehaviorRequest;
import com.tmc.sim.turnstile.api.SimDtos.BehaviorResponse;
import com.tmc.sim.turnstile.application.BehaviorHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Điều khiển hành vi khi trình diễn ({@code deploy/scripts/sim.sh}) – FR-08. */
@RestController
@RequestMapping("/admin/behavior")
public class BehaviorAdminController {

    private final BehaviorHolder behavior;

    public BehaviorAdminController(BehaviorHolder behavior) {
        this.behavior = behavior;
    }

    @GetMapping
    public BehaviorResponse get() {
        return BehaviorResponse.from(behavior.get());
    }

    @PutMapping
    public BehaviorResponse update(@RequestBody BehaviorRequest request) {
        Long min = request.latencyMs() == null ? null : request.latencyMs().get(0);
        Long max = request.latencyMs() == null ? null : request.latencyMs().get(1);
        return BehaviorResponse.from(behavior.update(min, max, request.failureRate(), request.mode()));
    }
}
