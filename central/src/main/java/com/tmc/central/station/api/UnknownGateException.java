package com.tmc.central.station.api;

/** Gate chưa được đăng ký ở C3. Đặt ở {@code api} vì module gọi {@link StationApi} cần bắt được. */
public class UnknownGateException extends RuntimeException {

    public UnknownGateException(String gateId) {
        super("Gate chưa đăng ký: " + gateId);
    }
}
