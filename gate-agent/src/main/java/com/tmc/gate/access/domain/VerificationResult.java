package com.tmc.gate.access.domain;

import java.util.Objects;

/** Kết quả {@link QrTokenVerifier#verify}: hoặc token hợp lệ, hoặc lý do từ chối. */
public sealed interface VerificationResult permits VerificationResult.Ok, VerificationResult.Fail {

    record Ok(VerifiedToken token) implements VerificationResult {
        public Ok {
            Objects.requireNonNull(token, "token");
        }
    }

    record Fail(RejectReason reason) implements VerificationResult {
        public Fail {
            Objects.requireNonNull(reason, "reason");
        }
    }
}
