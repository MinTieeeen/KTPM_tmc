package com.tmc.central.employee.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Bộ phận, thuộc một nhà máy. Dữ liệu seed (FR-01 đồng bộ HR ngoài MVP). */
@Entity
@Table(name = "departments")
public class Department {

    @Id
    @Column(name = "id", length = 16)
    private String id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "factory_id", nullable = false, length = 8)
    private String factoryId;

    protected Department() {
        // JPA
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public String factoryId() {
        return factoryId;
    }
}
