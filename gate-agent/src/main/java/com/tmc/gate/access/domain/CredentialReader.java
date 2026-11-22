package com.tmc.gate.access.domain;

import java.util.Optional;
import java.util.UUID;

/** Đọc bản sao credential cục bộ – chỉ đọc (ISP); G4 ghi qua {@code CredentialWriter}. */
public interface CredentialReader {

    Optional<CredentialEntry> findById(UUID credentialId);
}
