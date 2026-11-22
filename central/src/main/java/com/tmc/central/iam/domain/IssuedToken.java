package com.tmc.central.iam.domain;

import java.time.Instant;

/** Token truy cập đã phát. */
public record IssuedToken(String accessToken, Instant expiresAt) {

    @Override
    public String toString() {
        return "IssuedToken[expiresAt=" + expiresAt + "]";
    }
}
