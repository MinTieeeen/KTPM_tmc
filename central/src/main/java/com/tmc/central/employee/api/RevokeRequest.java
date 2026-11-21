package com.tmc.central.employee.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Body của {@code POST /employees/{code}/qr/revoke}; {@code reason} được cắt khoảng trắng trước khi kiểm. */
public record RevokeRequest(@NotBlank @Size(min = 3, max = 200) String reason) {

    public RevokeRequest {
        reason = reason == null ? null : reason.trim();
    }
}
