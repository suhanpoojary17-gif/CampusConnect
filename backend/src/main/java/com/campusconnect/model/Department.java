package com.campusconnect.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

@Entity
@Table(name = "departments")
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank(message = "Department name is required")
    @Column(nullable = false, unique = true)
    private String name;

    @NotBlank(message = "Department code is required")
    @Column(nullable = false, unique = true)
    private String code;

    public Department() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}