package com.tmc.central.employee.infrastructure;

import com.tmc.central.employee.domain.QrCredential;
import com.tmc.central.employee.domain.QrCredentialRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/** Spring Data cài đặt port {@link QrCredentialRepository} (DIP). */
interface JpaQrCredentialRepository extends JpaRepository<QrCredential, UUID>, QrCredentialRepository {
}
