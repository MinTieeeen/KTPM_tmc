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
import java.util.Optional;

/**
 * Cấp mới hoặc cấp lại credential (FR-04, INV-01): trong một transaction, thu hồi credential ACTIVE hiện có
 * (lý do {@code REISSUED}) rồi tạo credential ACTIVE mới. Hai lệnh cấp đồng thời → một lệnh vi phạm
 * {@code ux_one_active} → {@code 409 CONCURRENT_UPDATE}. Sự kiện {@link QrCredentialChanged} được giao sau commit.
 */
@Service
public class IssueQrCredentialUseCase {

    private static final Logger log = LoggerFactory.getLogger(IssueQrCredentialUseCase.class);

    private final EmployeeRepository employees;
    private final QrCredentialRepository credentials;
    private final ApplicationEventPublisher events;
    private final Clock clock;

    public IssueQrCredentialUseCase(EmployeeRepository employees, QrCredentialRepository credentials,
                                    ApplicationEventPublisher events, Clock clock) {
        this.employees = employees;
        this.credentials = credentials;
        this.events = events;
        this.clock = clock;
    }

    @Transactional
    public void issue(String employeeCode) {
        Employee employee = employees.findByCode(employeeCode)
                .orElseThrow(() -> new EmployeeNotFoundException(employeeCode));
        if (!employee.active()) {
            throw new CredentialUnavailableException(CredentialUnavailableException.EMPLOYEE_INACTIVE,
                    "Không cấp mã QR cho nhân viên đã nghỉ " + employeeCode);
        }
        Instant now = clock.instant();

        Optional<QrCredential> current = credentials.findByEmployeeCodeAndStatus(employeeCode, CredentialStatus.ACTIVE);
        if (current.isPresent()) {
            QrCredential old = current.get();
            old.revoke(QrCredential.REASON_REISSUED, now);
            credentials.save(old);
            credentials.flush(); // thu hồi trước khi chèn credential mới (ux_one_active)
            events.publishEvent(new QrCredentialChanged(old.id(), employeeCode, CredentialStatus.REVOKED.name(),
                    employee.active(), now));
        }

        QrCredential fresh = credentials.save(QrCredential.issue(employeeCode, now));
        credentials.flush(); // phát hiện vi phạm ux_one_active ngay trong use case
        events.publishEvent(new QrCredentialChanged(fresh.id(), employeeCode, CredentialStatus.ACTIVE.name(),
                employee.active(), now));
        log.info("Đã cấp credential {} cho {}", fresh.id(), employeeCode);
    }
}
