package com.tmc.central.employee.infrastructure;

import com.tmc.central.employee.domain.Department;
import com.tmc.central.employee.domain.DepartmentRepository;
import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data cài đặt port {@link DepartmentRepository} (DIP). */
interface JpaDepartmentRepository extends JpaRepository<Department, String>, DepartmentRepository {
}
