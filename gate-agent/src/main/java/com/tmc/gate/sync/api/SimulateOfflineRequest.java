package com.tmc.gate.sync.api;

import jakarta.validation.constraints.NotNull;

/** Body của {@code POST /admin/simulate-offline}. */
public record SimulateOfflineRequest(@NotNull Boolean offline) {
}
