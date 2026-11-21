package com.tmc.gate.sync.api;

import com.tmc.gate.sync.domain.NetworkFaultSwitch;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * {@code POST /admin/simulate-offline} – bật/tắt giả lập mất kết nối Central (FR-08, demo D4/T11). Bean chỉ tồn tại
 * khi {@code tmc.gate.fault-injection.enabled=true} (profile demo); ngược lại endpoint trả 404.
 */
@RestController
@ConditionalOnProperty(prefix = "tmc.gate.fault-injection", name = "enabled", havingValue = "true")
public class FaultInjectionController {

    private static final Logger log = LoggerFactory.getLogger(FaultInjectionController.class);

    private final NetworkFaultSwitch faultSwitch;

    public FaultInjectionController(NetworkFaultSwitch faultSwitch) {
        this.faultSwitch = faultSwitch;
    }

    @PostMapping("/admin/simulate-offline")
    public ResponseEntity<Void> simulateOffline(@Valid @RequestBody SimulateOfflineRequest request) {
        faultSwitch.setOffline(request.offline());
        log.warn("Giả lập mất kết nối Central: {}", request.offline() ? "BẬT" : "TẮT");
        return ResponseEntity.noContent().build();
    }
}
