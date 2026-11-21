package com.tmc.central.employee.infrastructure;

import com.tmc.central.employee.domain.SigningKey;
import com.tmc.central.employee.domain.SigningKeyRepository;
import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data cài đặt port {@link SigningKeyRepository} (DIP). */
interface JpaSigningKeyRepository extends JpaRepository<SigningKey, String>, SigningKeyRepository {
}
