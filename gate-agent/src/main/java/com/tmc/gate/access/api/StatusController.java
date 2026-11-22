package com.tmc.gate.access.api;

import com.tmc.gate.access.application.GetGateStatusUseCase;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** {@code GET /status} – kiosk hỏi mỗi 3 s để hiện biểu ngữ OFFLINE (NFR-AVL-04). */
@RestController
public class StatusController {

    private final GetGateStatusUseCase getStatus;

    public StatusController(GetGateStatusUseCase getStatus) {
        this.getStatus = getStatus;
    }

    @GetMapping("/status")
    public StatusResponse status() {
        return StatusResponse.from(getStatus.get());
    }
}
