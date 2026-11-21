package com.tmc.gate.access.domain;

import java.util.Objects;
import java.util.UUID;

/** Kết quả của {@link AccessRules#evaluate}. */
public sealed interface Decision permits Decision.Allow, Decision.Deny {

    /** Cảnh báo khi nhân viên quét RA sau một lượt RA (cho qua – DP-08). */
    String WARNING_OUT_AFTER_OUT = "OUT_AFTER_OUT";

    /**
     * Cho qua.
     *
     * @param warning {@code null} hoặc {@link #WARNING_OUT_AFTER_OUT}
     */
    record Allow(String employeeCode, Direction direction, UUID credentialId, String warning) implements Decision {
        public Allow {
            Objects.requireNonNull(employeeCode, "employeeCode");
            Objects.requireNonNull(direction, "direction");
            Objects.requireNonNull(credentialId, "credentialId");
        }
    }

    record Deny(RejectReason reason) implements Decision {
        public Deny {
            Objects.requireNonNull(reason, "reason");
        }
    }
}
