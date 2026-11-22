package com.tmc.sim.turnstile.api;

import com.tmc.sim.turnstile.api.SimDtos.CommandStatusResponse;
import com.tmc.sim.turnstile.api.SimDtos.OpenRequest;
import com.tmc.sim.turnstile.api.SimDtos.OpenResponse;
import com.tmc.sim.turnstile.api.SimDtos.TurnstileStatusResponse;
import com.tmc.sim.turnstile.application.TurnstileSimulatorService;
import com.tmc.sim.turnstile.domain.Turnstile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/** API cổng xoay mà G2 {@code HttpTurnstileAdapter} gọi. */
@RestController
@RequestMapping("/turnstiles")
public class TurnstileController {

    private final TurnstileSimulatorService service;

    public TurnstileController(TurnstileSimulatorService service) {
        this.service = service;
    }

    @PostMapping("/{id}/open")
    public OpenResponse open(@PathVariable String id, @RequestBody OpenRequest request) {
        return OpenResponse.from(service.open(id, request.requestId()));
    }

    @GetMapping("/{id}/commands/{requestId}")
    public CommandStatusResponse command(@PathVariable String id, @PathVariable UUID requestId) {
        return service.commandExecutedAt(id, requestId)
                .map(at -> new CommandStatusResponse(true, at))
                .orElseGet(() -> new CommandStatusResponse(false, null));
    }

    @GetMapping("/{id}/status")
    public TurnstileStatusResponse status(@PathVariable String id) {
        return toStatus(service.status(id));
    }

    @GetMapping
    public List<TurnstileStatusResponse> all() {
        return service.all().stream().map(this::toStatus).toList();
    }

    private TurnstileStatusResponse toStatus(Turnstile t) {
        return new TurnstileStatusResponse(t.id(), service.health(), t.openCount());
    }
}
