package com.tmc.central.employee.application;

import com.tmc.central.employee.api.EmployeeView;
import com.tmc.central.employee.domain.Employee;
import com.tmc.central.employee.domain.EmployeeNotFoundException;
import com.tmc.central.employee.domain.EmployeeRepository;
import com.tmc.central.employee.domain.QrCredential;
import com.tmc.central.employee.domain.QrCredentialRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

/**
 * Danh sách nhân viên kèm credential mới nhất cho Admin (FR-09). Dữ liệu demo nhỏ (20 nhân viên) nên lọc và ghép
 * trong bộ nhớ; sắp theo mã tăng dần.
 */
@Service
public class ListEmployeesQuery {

    private final EmployeeRepository employees;
    private final QrCredentialRepository credentials;

    public ListEmployeesQuery(EmployeeRepository employees, QrCredentialRepository credentials) {
        this.employees = employees;
        this.credentials = credentials;
    }

    /**
     * @param departmentId lọc đúng mã bộ phận; {@code null}/rỗng = mọi bộ phận
     * @param query        chuỗi con của mã hoặc họ tên, không phân biệt hoa thường; {@code null}/rỗng = không lọc
     */
    @Transactional(readOnly = true)
    public List<EmployeeView> list(String departmentId, String query) {
        Map<String, QrCredential> latest = latestCredentialByEmployee();
        String q = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        return employees.findAll().stream()
                .filter(e -> departmentId == null || departmentId.isBlank()
                        || e.department().id().equals(departmentId))
                .filter(e -> q.isEmpty()
                        || e.code().toLowerCase(Locale.ROOT).contains(q)
                        || e.fullName().toLowerCase(Locale.ROOT).contains(q))
                .sorted(Comparator.comparing(Employee::code))
                .map(e -> toView(e, latest.get(e.code())))
                .toList();
    }

    @Transactional(readOnly = true)
    public EmployeeView view(String employeeCode) {
        Employee employee = employees.findByCode(employeeCode)
                .orElseThrow(() -> new EmployeeNotFoundException(employeeCode));
        QrCredential latest = credentials.findFirstByEmployeeCodeOrderByIssuedAtDesc(employeeCode).orElse(null);
        return toView(employee, latest);
    }

    private Map<String, QrCredential> latestCredentialByEmployee() {
        BinaryOperator<QrCredential> newer = (a, b) -> a.issuedAt().isAfter(b.issuedAt()) ? a : b;
        return credentials.findAll().stream()
                .collect(Collectors.toMap(QrCredential::employeeCode, c -> c, newer));
    }

    private static EmployeeView toView(Employee e, QrCredential latest) {
        return new EmployeeView(
                e.code(),
                e.fullName(),
                e.department().id(),
                e.department().name(),
                e.department().factoryId(),
                e.active(),
                latest == null ? null : latest.id(),
                latest == null ? null : latest.status().name(),
                latest == null ? null : latest.issuedAt());
    }
}
