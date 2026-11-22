package com.tmc.central.employee.domain;

import java.util.List;
import java.util.Optional;

/** Kho bộ phận (cài đặt: JpaDepartmentRepository). */
public interface DepartmentRepository {

    Optional<Department> findById(String id);

    List<Department> findAll();
}
