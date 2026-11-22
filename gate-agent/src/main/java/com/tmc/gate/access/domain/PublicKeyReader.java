package com.tmc.gate.access.domain;

import java.security.PublicKey;
import java.util.Optional;

/** Đọc khóa công khai Ed25519 đã đồng bộ xuống; chỉ trả khóa {@code active=true}. */
public interface PublicKeyReader {

    Optional<PublicKey> findActive(String kid);
}
