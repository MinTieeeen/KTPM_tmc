package com.tmc.central.ingestion.domain;

import java.util.Objects;

/** Gate đã xác thực bằng API key và nhà máy của nó (tra ở C3). Không tin dữ liệu khai trong body (DP-11). */
public record GateContext(String gateId, String factoryId) {

    public GateContext {
        Objects.requireNonNull(gateId, "gateId");
        Objects.requireNonNull(factoryId, "factoryId");
    }
}
