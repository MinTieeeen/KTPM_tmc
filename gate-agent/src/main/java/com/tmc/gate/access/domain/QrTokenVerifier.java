package com.tmc.gate.access.domain;

import java.time.Instant;

/**
 * Kiểm định dạng, {@code kid}, chữ ký và thời hạn của chuỗi QR (quy tắc 1, 2).
 * <p>Hợp đồng: không ném ngoại lệ; mọi lỗi trả {@link VerificationResult.Fail} với lý do thuộc
 * {INVALID_FORMAT, UNKNOWN_KEY, BAD_SIGNATURE, EXPIRED, NOT_YET_VALID}.
 */
public interface QrTokenVerifier {

    VerificationResult verify(String raw, Instant now);
}
