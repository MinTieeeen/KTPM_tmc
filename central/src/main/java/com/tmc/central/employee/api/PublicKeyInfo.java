package com.tmc.central.employee.api;

/**
 * Khóa công khai ký QR.
 *
 * @param publicKeyBase64 X.509 SubjectPublicKeyInfo, base64 chuẩn
 */
public record PublicKeyInfo(String kid, String publicKeyBase64, boolean active) {
}
