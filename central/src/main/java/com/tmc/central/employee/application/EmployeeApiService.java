package com.tmc.central.employee.application;

import com.tmc.central.employee.api.CredentialState;
import com.tmc.central.employee.api.EmployeeApi;
import com.tmc.central.employee.api.PublicKeyInfo;
import com.tmc.central.employee.domain.Employee;
import com.tmc.central.employee.domain.EmployeeRepository;
import com.tmc.central.employee.domain.QrCredential;
import com.tmc.central.employee.domain.QrCredentialRepository;
import com.tmc.central.employee.domain.SigningKeyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Cài đặt {@link EmployeeApi} cho module khác (C1). */
@Service
public class EmployeeApiService implements EmployeeApi {

    private final EmployeeRepository employees;
    private final QrCredentialRepository credentials;
    private final SigningKeyRepository signingKeys;

    public EmployeeApiService(EmployeeRepository employees, QrCredentialRepository credentials,
                              SigningKeyRepository signingKeys) {
        this.employees = employees;
        this.credentials = credentials;
        this.signingKeys = signingKeys;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CredentialState> allCredentialStates() {
        Map<String, Boolean> activeByCode = employees.findAll().stream()
                .collect(Collectors.toMap(Employee::code, Employee::active));
        BinaryOperator<QrCredential> newer = (a, b) -> a.issuedAt().isAfter(b.issuedAt()) ? a : b;
        return credentials.findAll().stream()
                .collect(Collectors.toMap(QrCredential::employeeCode, Function.identity(), newer))
                .values().stream()
                .sorted(Comparator.comparing(QrCredential::employeeCode))
                .map(c -> new CredentialState(c.id(), c.employeeCode(), c.status().name(),
                        activeByCode.getOrDefault(c.employeeCode(), false), c.updatedAt()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PublicKeyInfo> publicKeys() {
        return signingKeys.findAll().stream()
                .map(k -> new PublicKeyInfo(k.kid(), Base64.getEncoder().encodeToString(k.publicKey()), k.active()))
                .toList();
    }
}
