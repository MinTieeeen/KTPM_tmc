package com.tmc.contract.qr;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.UUID;

/**
 * Payload của mã QR động (SRS-SC-FR-10, ADR-02). Không chứa mã nhân viên hay họ tên (DP-12).
 *
 * @param v     phiên bản định dạng, hiện tại {@link QrTokenFormat#FORMAT_VERSION}
 * @param cid   credentialId
 * @param iat   thời điểm phát hành, epoch giây
 * @param exp   thời điểm hết hạn, epoch giây ({@code iat + TTL})
 * @param nonce base64url của 16 byte ngẫu nhiên, dùng một lần
 * @param kid   mã khóa ký Ed25519
 */
@JsonPropertyOrder({"v", "cid", "iat", "exp", "nonce", "kid"})
public record QrTokenPayload(
        int v,
        UUID cid,
        long iat,
        long exp,
        String nonce,
        String kid) {
}
