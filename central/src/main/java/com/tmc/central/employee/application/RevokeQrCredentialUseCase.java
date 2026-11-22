package com.tmc.central.employee.application;

import com.tmc.central.employee.api.QrCredentialChanged;
import com.tmc.central.employee.domain.CredentialStatus;
import com.tmc.central.employee.domain.CredentialUnavailableException;
import com.tmc.central.employee.domain.Employee;
import com.tmc.central.employee.domain.EmployeeNotFoundException;
import com.tmc.central.employee.domain.EmployeeRepository;
import com.tmc.central.employee.domain.QrCredential;
import com.tmc.central.employee.domain.QrCredentialRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

/**
 * Thu hồi credential ACTIVE (FR-05). Có hiệu lực tại gate sau ≤ 60 s: sự kiện → C1 ghi delta → G4 kéo
 * (NFR-SEC-06); Portal nhận {@code 409 CREDENTIAL_REVOKED} ở lần làm mới kế tiếp.
 */
@Service
public class RevokeQrCredentialUseCase {

    private static final Logger log = LoggerFactory.getLogger(RevokeQrCredentialUseCase.class);

    private final EmployeeRepository employees;
    private final QrCredentialRepository credentials;
    private final ApplicationEventPublisher events;
    private final Clock clock;

    public RevokeQrCredentialUseCase(EmployeeRepository employees, QrCredentialRepository credentials,
                                     ApplicationEventPublisher events, Clock clock) {
        this.employees = employees;
        this.credentials = credentials;
        this.events = events;
        this.clock = clock;
    }

    @Transactional
    public void revoke(String employeeCode, String reason) {
        Employee employee = employees.findByCode(employeeCode)
                .orElseThrow(() -> new EmployeeNotFoundException(employeeCode));
        QrCredential credential = credentials.findByEmployeeCodeAndStatus(employeeCode, CredentialStatus.ACTIVE)
                .orElseThrow(() -> new CredentialUnavailableException(CredentialUnavailableException.NO_ACTIVE_CREDENTIAL,
                        "Nhân viên " + employeeCode + " không có credential đang hoạt động"));

        Instant now = clock.instant();
        credential.revoke(reason, now);
        credentials.save(credential);
        events.publishEvent(new QrCredentialChanged(credential.id(), employeeCode, CredentialStatus.REVOKED.name(),
                employee.active(), now));
        log.info("Đã thu hồi credential {} của {}", credential.id(), employeeCode);
    }
}
