package com.tmc.gate.guard.api;

import com.tmc.gate.guard.application.PendingReconcileService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * {@code GET /guard/pending} – G5 rút gọn (D1): lượt PENDING còn lại sau khi khởi động lại. Không yêu cầu xác thực
 * vì chỉ phục vụ LAN nhà máy trong demo (D3).
 */
@RestController
public class PendingReconcileController {

    private final PendingReconcileService service;

    public PendingReconcileController(PendingReconcileService service) {
        this.service = service;
    }

    @GetMapping("/guard/pending")
    public List<PendingScanView> pending() {
        return service.listPending().stream().map(PendingScanView::from).toList();
    }
}
