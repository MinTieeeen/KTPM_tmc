package com.tmc.central.station.api;

/** Gate Agent và nhà máy nó phục vụ (mỗi nhà máy một gate). */
public record GateInfo(String gateId, String factoryId) {
}
