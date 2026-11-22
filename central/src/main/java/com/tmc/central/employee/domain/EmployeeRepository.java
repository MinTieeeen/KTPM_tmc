package com.tmc.central.employee.domain;

import java.util.List;
import java.util.Optional;

/** Kho nhân viên (cài đặt: JpaEmployeeRepository). */
public interface EmployeeRepository {

    Optional<Employee> findByCode(String code);

    List<Employee> findAll();
}
