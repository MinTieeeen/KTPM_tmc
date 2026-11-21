package com.tmc.central.employee.domain;

/** Không có nhân viên với mã đã cho ({@code 404 EMPLOYEE_NOT_FOUND}). */
public class EmployeeNotFoundException extends RuntimeException {

    private final String employeeCode;

    public EmployeeNotFoundException(String employeeCode) {
        super("Không tìm thấy nhân viên " + employeeCode);
        this.employeeCode = employeeCode;
    }

    public String employeeCode() {
        return employeeCode;
    }
}
