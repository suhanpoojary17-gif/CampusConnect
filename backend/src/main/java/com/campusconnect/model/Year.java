package com.campusconnect.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

@Entity
@Table(name = "academic_years")
public class Year {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank(message = "Year name is required")
    @Column(nullable = false, unique = true)
    private String name;

    public Year() {
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
}