package com.tmc.central.employee.domain;

/** Ký mã QR động (ADR-02; cài đặt: Ed25519QrTokenSigner). */
public interface QrTokenSigner {

    /** Sinh nonce mới, dựng payload và ký. */
    SignedQrToken sign(QrTokenClaims claims);

    /** Mã khóa ký đang dùng ({@code kid}). */
    String keyId();

    /** Khóa công khai đang dùng dạng X.509 SubjectPublicKeyInfo. */
    byte[] publicKeyX509();
}
