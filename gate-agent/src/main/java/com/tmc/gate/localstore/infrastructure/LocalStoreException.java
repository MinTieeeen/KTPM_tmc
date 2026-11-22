package com.tmc.gate.localstore.infrastructure;

import org.springframework.dao.NonTransientDataAccessException;

/**
 * Lỗi lưu trữ cục bộ có ngữ cảnh (ví dụ không tuần tự hóa được payload outbox). Là một
 * {@code DataAccessException} để tầng api xử lý chung với mọi lỗi SQLite ({@code LOCAL_STORE_ERROR}).
 */
public class LocalStoreException extends NonTransientDataAccessException {

    public LocalStoreException(String message, Throwable cause) {
        super(message, cause);
    }
}
