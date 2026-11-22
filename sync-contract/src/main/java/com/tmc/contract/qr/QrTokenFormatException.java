package com.tmc.contract.qr;

/**
 * Lỗi định dạng chuỗi QR (mục 6.7). Không dùng cho lỗi chữ ký hay thời hạn – các lỗi đó do G1 kiểm.
 */
public class QrTokenFormatException extends RuntimeException {

    public enum Code {
        /** Sai tiền tố, sai số phần, base64 hỏng, chữ ký sai độ dài, JSON hỏng hoặc thiếu trường. */
        INVALID_FORMAT,
        /** Trường {@code v} khác {@link QrTokenFormat#FORMAT_VERSION}. */
        UNSUPPORTED_VERSION
    }

    private final Code code;

    public QrTokenFormatException(Code code, String message) {
        super(message);
        this.code = code;
    }

    public QrTokenFormatException(Code code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public Code code() {
        return code;
    }
}
