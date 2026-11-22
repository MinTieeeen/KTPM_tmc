package com.tmc.central.ingestion.domain;

/** Cả yêu cầu đồng bộ bị từ chối (mục 6.7); khác với sự kiện lẻ bị từ chối nằm trong {@code BatchAck.rejected}. */
public class SyncRejectedException extends RuntimeException {

    public enum Reason {
        /** 400 */
        UNSUPPORTED_CONTRACT_VERSION,
        /** 400 */
        BATCH_TOO_LARGE,
        /** 403 – {@code gateId} trong body khác gate đã xác thực */
        GATE_MISMATCH,
        /** 403 – gate có API key nhưng chưa đăng ký ở C3 */
        UNKNOWN_GATE
    }

    private final Reason reason;

    public SyncRejectedException(Reason reason, String message) {
        super(message);
        this.reason = reason;
    }

    public Reason reason() {
        return reason;
    }
}
