package com.tmc.central.iam.api;

/** Principal của một Gate Agent đã xác thực bằng {@code X-Gate-Key} trên {@code /sync/**}. */
public record GatePrincipal(String gateId) {
}
