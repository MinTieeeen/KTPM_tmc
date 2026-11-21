package com.tmc.contract.qr;

import java.util.Arrays;
import java.util.Objects;

/**
 * Kết quả {@link QrTokenFormat#parse(String)}. Bên kiểm tra (G1) kiểm chữ ký trên chính
 * {@link #payloadJson()} – không tuần tự hóa lại payload.
 *
 * @param payloadJson byte JSON của payload đúng như đã được ký
 * @param signature   chữ ký Ed25519 (64 byte)
 * @param payload     payload đã giải mã
 */
public record QrTokenParts(byte[] payloadJson, byte[] signature, QrTokenPayload payload) {

    public QrTokenParts {
        Objects.requireNonNull(payloadJson, "payloadJson");
        Objects.requireNonNull(signature, "signature");
        Objects.requireNonNull(payload, "payload");
        payloadJson = payloadJson.clone();
        signature = signature.clone();
    }

    @Override
    public byte[] payloadJson() {
        return payloadJson.clone();
    }

    @Override
    public byte[] signature() {
        return signature.clone();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof QrTokenParts other
                && Arrays.equals(payloadJson, other.payloadJson)
                && Arrays.equals(signature, other.signature)
                && payload.equals(other.payload);
    }

    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(payloadJson), Arrays.hashCode(signature), payload);
    }

    @Override
    public String toString() {
        return "QrTokenParts[payload=" + payload + ", payloadBytes=" + payloadJson.length
                + ", signatureBytes=" + signature.length + "]";
    }
}
