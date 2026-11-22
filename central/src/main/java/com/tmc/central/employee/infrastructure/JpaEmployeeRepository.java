package com.tmc.central.employee.infrastructure;

import com.tmc.central.employee.domain.Employee;
import com.tmc.central.employee.domain.EmployeeRepository;
import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data cài đặt port {@link EmployeeRepository} (DIP). */
interface JpaEmployeeRepository extends JpaRepository<Employee, String>, EmployeeRepository {
}
