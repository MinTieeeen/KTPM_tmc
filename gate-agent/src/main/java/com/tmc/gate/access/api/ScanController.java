package com.tmc.gate.access.api;

import com.tmc.gate.access.application.ProcessScanUseCase;
import com.tmc.gate.access.application.ScanCommand;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.Clock;
import java.time.Instant;

/**
 * {@code POST /scan} – nhận lượt quét từ kiosk (không cần tài khoản, ADR-08). Trả {@code 200} cho cả ba kết quả
 * nghiệp vụ; không chứa quy tắc nào.
 */
@RestController
public class ScanController {

    private final ProcessScanUseCase processScan;
    private final Clock clock;

    public ScanController(ProcessScanUseCase processScan, Clock clock) {
        this.processScan = processScan;
        this.clock = clock;
    }

    @PostMapping("/scan")
    public ScanResponse scan(@Valid @RequestBody ScanRequest request) {
        Instant receivedAt = clock.instant();
        return ScanResponse.from(processScan.handle(new ScanCommand(request.stationId(), request.raw(), receivedAt)));
    }
}
