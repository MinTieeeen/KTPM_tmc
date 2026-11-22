package com.tmc.central.employee.infrastructure;

import com.tmc.central.employee.application.QrProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * Cặp khóa Ed25519 ký QR (ADR-02, D4). Nạp từ {@code TMC_QR_SIGNING_KEY} (PKCS#8 base64) và
 * {@code TMC_QR_PUBLIC_KEY} (X.509 base64); profile demo tự sinh khi để trống. Thiếu khóa ngoài demo → không khởi
 * động. Khóa bí mật chỉ nằm trong bộ nhớ tiến trình, không ghi CSDL, không log (DP-11).
 */
@Component
public class QrSigningKeyProvider {

    private static final Logger log = LoggerFactory.getLogger(QrSigningKeyProvider.class);
    private static final String ALGORITHM = "Ed25519";

    private final String keyId;
    private final PrivateKey privateKey;
    private final PublicKey publicKey;

    public QrSigningKeyProvider(QrProperties props,
                                @Value("${tmc.central.demo.auto-generate-keys:false}") boolean autoGenerate) {
        this.keyId = props.keyId();
        try {
            if (!props.signingKey().isBlank() && !props.publicKey().isBlank()) {
                KeyFactory factory = KeyFactory.getInstance(ALGORITHM);
                this.privateKey = factory.generatePrivate(
                        new PKCS8EncodedKeySpec(Base64.getDecoder().decode(props.signingKey().trim())));
                this.publicKey = factory.generatePublic(
                        new X509EncodedKeySpec(Base64.getDecoder().decode(props.publicKey().trim())));
                log.info("Đã nạp khóa ký QR kid={} từ cấu hình", keyId);
            } else if (autoGenerate) {
                KeyPair pair = KeyPairGenerator.getInstance(ALGORITHM).generateKeyPair();
                this.privateKey = pair.getPrivate();
                this.publicKey = pair.getPublic();
                log.warn("Chưa cấu hình khóa ký QR – đã tự sinh khóa Ed25519 kid={} (chỉ dùng cho demo)", keyId);
            } else {
                throw new IllegalStateException(
                        "Thiếu khóa ký QR: đặt TMC_QR_SIGNING_KEY và TMC_QR_PUBLIC_KEY, hoặc bật tmc.central.demo.auto-generate-keys");
            }
        } catch (GeneralSecurityException | IllegalArgumentException e) {
            throw new IllegalStateException("Không đọc được khóa ký QR", e);
        }
    }

    public String keyId() {
        return keyId;
    }

    PrivateKey privateKey() {
        return privateKey;
    }

    /** X.509 SubjectPublicKeyInfo. */
    public byte[] publicKeyX509() {
        return publicKey.getEncoded();
    }
}
