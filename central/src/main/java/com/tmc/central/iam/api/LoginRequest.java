package com.tmc.central.iam.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Body của {@code POST /auth/login}. */
public record LoginRequest(
        @NotBlank @Size(max = 32) String username,
        @NotBlank @Size(max = 128) String password) {

    /** Không để lộ mật khẩu trong log (NFR-PRI-03). */
    @Override
    public String toString() {
        return "LoginRequest[username=" + username + "]";
    }
}
