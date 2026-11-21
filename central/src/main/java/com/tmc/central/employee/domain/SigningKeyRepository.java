package com.tmc.central.employee.domain;

import java.util.List;
import java.util.Optional;

/** Kho khóa công khai ký QR (cài đặt: JpaSigningKeyRepository). */
public interface SigningKeyRepository {

    Optional<SigningKey> findByKid(String kid);

    List<SigningKey> findAll();

    SigningKey save(SigningKey key);
}
