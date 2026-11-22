package com.tmc.contract.snapshot;

/** Tham số vận hành gửi xuống gate (thay module C9). */
public record GateSettingsDto(int offlineThresholdMinutes) {
}
