package com.tmc.gate.access.infrastructure;

import com.tmc.contract.qr.QrTokenFormat;
import com.tmc.contract.qr.QrTokenFormatException;
import com.tmc.contract.qr.QrTokenParts;
import com.tmc.contract.qr.QrTokenPayload;
import com.tmc.gate.access.domain.PublicKeyReader;
import com.tmc.gate.access.domain.QrTokenVerifier;
import com.tmc.gate.access.domain.RejectReason;
import com.tmc.gate.access.domain.VerificationResult;
import com.tmc.gate.access.domain.VerifiedToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.security.Signature;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

/**
 * Kiểm mã QR động bằng khóa công khai Ed25519 đã đồng bộ xuống (ADR-02, NFR-SEC-02, NFR-SEC-05).
 * Thứ tự: định dạng → {@code kid} → chữ ký (trên đúng byte payload đã ký) → thời hạn ±clockSkew.
 * Không bao giờ log chuỗi {@code raw} (NFR-PRI-03).
 */
@Component
public class Ed25519TokenVerifier implements QrTokenVerifier {

    private static final Logger log = LoggerFactory.getLogger(Ed25519TokenVerifier.class);
    private static final String ALGORITHM = "Ed25519";

    private final PublicKeyReader publicKeys;
    private final Duration clockSkew;

    public Ed25519TokenVerifier(PublicKeyReader publicKeys, AccessProperties props) {
        this.publicKeys = publicKeys;
        this.clockSkew = props.qr().clockSkew();
    }

    @Override
    public VerificationResult verify(String raw, Instant now) {
        QrTokenParts parts;
        try {
            parts = QrTokenFormat.parse(raw);
        } catch (QrTokenFormatException e) {
            // UNSUPPORTED_VERSION cũng là "không đọc được" đối với kiosk
            return fail(RejectReason.INVALID_FORMAT);
        }
        QrTokenPayload payload = parts.payload();

        Optional<PublicKey> key = publicKeys.findActive(payload.kid());
        if (key.isEmpty()) {
            return fail(RejectReason.UNKNOWN_KEY);
        }
        if (!signatureValid(key.get(), parts)) {
            return fail(RejectReason.BAD_SIGNATURE);
        }

        Instant issuedAt = Instant.ofEpochSecond(payload.iat());
        Instant expiresAt = Instant.ofEpochSecond(payload.exp());
        if (now.isBefore(issuedAt.minus(clockSkew))) {
            return fail(RejectReason.NOT_YET_VALID);
        }
        if (now.isAfter(expiresAt.plus(clockSkew))) {
            return fail(RejectReason.EXPIRED);
        }
        return new VerificationResult.Ok(
                new VerifiedToken(payload.cid(), payload.nonce(), payload.kid(), issuedAt, expiresAt));
    }

    private static boolean signatureValid(PublicKey key, QrTokenParts parts) {
        try {
            Signature verifier = Signature.getInstance(ALGORITHM);
            verifier.initVerify(key);
            verifier.update(parts.payloadJson());
            return verifier.verify(parts.signature());
        } catch (GeneralSecurityException | IllegalArgumentException e) {
            log.debug("Kiểm chữ ký QR thất bại (kid={}): {}", parts.payload().kid(), e.getMessage());
            return false;
        }
    }

    private static VerificationResult fail(RejectReason reason) {
        return new VerificationResult.Fail(reason);
    }
}
