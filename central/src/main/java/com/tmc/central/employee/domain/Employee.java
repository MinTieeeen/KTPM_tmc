package com.tmc.central.employee.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** Nhân viên. Dữ liệu seed; MVP không có thao tác sửa hồ sơ (D6). */
@Entity
@Table(name = "employees")
public class Employee {

    @Id
    @Column(name = "code", length = 16)
    private String code;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @Column(name = "active", nullable = false)
    private boolean active;

    protected Employee() {
        // JPA
    }

    public String code() {
        return code;
    }

    public String fullName() {
        return fullName;
    }

    public Department department() {
        return department;
    }

    /** {@code false} khi nhân viên đã nghỉ việc (INV-02). */
    public boolean active() {
        return active;
    }
}
