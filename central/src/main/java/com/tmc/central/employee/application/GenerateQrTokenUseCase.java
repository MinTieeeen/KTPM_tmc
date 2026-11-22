package com.tmc.central.employee.application;

import com.tmc.central.employee.domain.CredentialStatus;
import com.tmc.central.employee.domain.CredentialUnavailableException;
import com.tmc.central.employee.domain.Employee;
import com.tmc.central.employee.domain.EmployeeNotFoundException;
import com.tmc.central.employee.domain.EmployeeRepository;
import com.tmc.central.employee.domain.QrCredential;
import com.tmc.central.employee.domain.QrCredentialRepository;
import com.tmc.central.employee.domain.QrTokenClaims;
import com.tmc.central.employee.domain.QrTokenSigner;
import com.tmc.central.employee.domain.SignedQrToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

/**
 * Sinh mã QR động cho nhân viên đang đăng nhập (FR-01 – FR-03). Mã nhân viên do caller lấy từ JWT, không nhận
 * từ tham số request, nên nhân viên chỉ lấy được mã của chính mình.
 */
@Service
public class GenerateQrTokenUseCase {

    private final EmployeeRepository employees;
    private final QrCredentialRepository credentials;
    private final QrTokenSigner signer;
    private final QrProperties props;
    private final Clock clock;

    public GenerateQrTokenUseCase(EmployeeRepository employees, QrCredentialRepository credentials,
                                  QrTokenSigner signer, QrProperties props, Clock clock) {
        this.employees = employees;
        this.credentials = credentials;
        this.signer = signer;
        this.props = props;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public SignedQrToken generateFor(String employeeCode) {
        Employee employee = employees.findByCode(employeeCode)
                .orElseThrow(() -> new EmployeeNotFoundException(employeeCode));
        if (!employee.active()) {
            throw new CredentialUnavailableException(CredentialUnavailableException.EMPLOYEE_INACTIVE,
                    "Nhân viên " + employeeCode + " không còn hoạt động");
        }
        QrCredential credential = credentials.findByEmployeeCodeAndStatus(employeeCode, CredentialStatus.ACTIVE)
                .orElseThrow(() -> noActiveCredential(employeeCode));

        Instant now = clock.instant();
        return signer.sign(new QrTokenClaims(credential.id(), now, now.plus(props.tokenTtl())));
    }

    private CredentialUnavailableException noActiveCredential(String employeeCode) {
        boolean revoked = credentials.findFirstByEmployeeCodeOrderByIssuedAtDesc(employeeCode)
                .map(c -> c.status() == CredentialStatus.REVOKED)
                .orElse(false);
        return revoked
                ? new CredentialUnavailableException(CredentialUnavailableException.CREDENTIAL_REVOKED,
                        "Credential của " + employeeCode + " đã bị thu hồi")
                : new CredentialUnavailableException(CredentialUnavailableException.NO_ACTIVE_CREDENTIAL,
                        "Nhân viên " + employeeCode + " chưa được cấp mã QR");
    }
}
