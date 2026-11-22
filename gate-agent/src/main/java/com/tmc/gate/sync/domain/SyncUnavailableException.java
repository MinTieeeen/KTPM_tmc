package com.tmc.gate.sync.domain;

/**
 * Không trao đổi được với Central: mất mạng, timeout, 5xx, 4xx hoặc đang giả lập offline. Tác vụ đồng bộ bỏ chu kỳ
 * này và thử lại ở chu kỳ sau; không mất dữ liệu (FR-03).
 */
public class SyncUnavailableException extends RuntimeException {

    /** Mã HTTP khi Central có trả lời; {@code 0} khi không có phản hồi (mạng, timeout, giả lập offline). */
    private final int httpStatus;

    public SyncUnavailableException(String message, int httpStatus, Throwable cause) {
        super(message, cause);
        this.httpStatus = httpStatus;
    }

    public SyncUnavailableException(String message) {
        this(message, 0, null);
    }

    public int httpStatus() {
        return httpStatus;
    }

    public boolean isAuthenticationFailure() {
        return httpStatus == 401 || httpStatus == 403;
    }

    public boolean isClientError() {
        return httpStatus >= 400 && httpStatus < 500;
    }
}
