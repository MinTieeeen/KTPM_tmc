package com.tmc.central.employee.infrastructure;

import com.tmc.central.employee.domain.QrTokenClaims;
import com.tmc.central.employee.domain.QrTokenSigner;
import com.tmc.central.employee.domain.SignedQrToken;
import com.tmc.contract.qr.QrTokenFormat;
import com.tmc.contract.qr.QrTokenPayload;
import org.springframework.stereotype.Component;

import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.Signature;
import java.util.Base64;

/**
 * Ký mã QR động bằng Ed25519 (ADR-02, FR-02): sinh nonce 16 byte → dựng payload → tuần tự hóa bằng
 * {@link QrTokenFormat#serializePayload} → ký trên đúng mảng byte đó → {@link QrTokenFormat#encode}.
 * Không log chuỗi QR (NFR-03).
 */
@Component
public class Ed25519QrTokenSigner implements QrTokenSigner {

    private static final int NONCE_BYTES = 16;

    private final QrSigningKeyProvider keys;
    private final SecureRandom random = new SecureRandom();
    private final Base64.Encoder nonceEncoder = Base64.getUrlEncoder().withoutPadding();

    public Ed25519QrTokenSigner(QrSigningKeyProvider keys) {
        this.keys = keys;
    }

    @Override
    public SignedQrToken sign(QrTokenClaims claims) {
        byte[] nonce = new byte[NONCE_BYTES];
        random.nextBytes(nonce);
        QrTokenPayload payload = new QrTokenPayload(
                QrTokenFormat.FORMAT_VERSION,
                claims.credentialId(),
                claims.issuedAt().getEpochSecond(),
                claims.expiresAt().getEpochSecond(),
                nonceEncoder.encodeToString(nonce),
                keys.keyId());
        byte[] payloadJson = QrTokenFormat.serializePayload(payload);
        try {
            Signature signature = Signature.getInstance("Ed25519");
            signature.initSign(keys.privateKey());
            signature.update(payloadJson);
            String raw = QrTokenFormat.encode(payloadJson, signature.sign());
            return new SignedQrToken(raw, claims.expiresAt());
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Không ký được mã QR", e);
        }
    }

    @Override
    public String keyId() {
        return keys.keyId();
    }

    @Override
    public byte[] publicKeyX509() {
        return keys.publicKeyX509();
    }
}
