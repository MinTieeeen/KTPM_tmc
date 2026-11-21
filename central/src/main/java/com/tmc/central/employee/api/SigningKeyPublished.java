package com.tmc.central.employee.api;

import java.time.Instant;

/**
 * Sự kiện: khóa công khai ký QR được công bố hoặc ngừng dùng (FR-08); C1 nghe để gửi xuống gate.
 *
 * @param publicKeyBase64 X.509 SubjectPublicKeyInfo, base64 chuẩn
 */
public record SigningKeyPublished(String kid, String publicKeyBase64, boolean active, Instant publishedAt) {
}
