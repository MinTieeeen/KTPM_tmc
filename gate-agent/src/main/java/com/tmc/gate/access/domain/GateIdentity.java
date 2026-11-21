package com.tmc.gate.access.domain;

/** Danh tính của Gate Agent: một gate phục vụ đúng một nhà máy. */
public record GateIdentity(String gateId, String factoryId) {
}
