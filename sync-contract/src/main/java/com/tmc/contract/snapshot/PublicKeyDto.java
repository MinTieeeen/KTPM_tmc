package com.tmc.contract.snapshot;

/**
 * Khóa công khai Ed25519 để G1 kiểm chữ ký QR.
 *
 * @param publicKeyBase64 X.509 SubjectPublicKeyInfo, base64 chuẩn
 */
public record PublicKeyDto(String kid, String publicKeyBase64, boolean active) {
}
