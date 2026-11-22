package com.tmc.central.employee.api;

import com.tmc.central.employee.application.GenerateQrTokenUseCase;
import com.tmc.central.employee.application.QrProperties;
import com.tmc.central.employee.domain.SignedQrToken;
import com.tmc.central.iam.api.AuthApi;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * {@code GET /portal/qr-token} (vai trò EMPLOYEE). Mã nhân viên lấy từ JWT qua {@link AuthApi}, không nhận tham số
 * – nhân viên chỉ lấy được mã của chính mình (FR-03, S3).
 */
@RestController
public class QrTokenController {

    private final GenerateQrTokenUseCase generateToken;
    private final AuthApi authApi;
    private final QrProperties props;

    public QrTokenController(GenerateQrTokenUseCase generateToken, AuthApi authApi, QrProperties props) {
        this.generateToken = generateToken;
        this.authApi = authApi;
        this.props = props;
    }

    @GetMapping("/portal/qr-token")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<QrTokenResponse> qrToken() {
        SignedQrToken token = generateToken.generateFor(authApi.currentEmployeeCode());
        QrTokenResponse body = new QrTokenResponse(token.raw(), token.expiresAt(), props.refreshAfter().toSeconds());
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(body);
    }
}
