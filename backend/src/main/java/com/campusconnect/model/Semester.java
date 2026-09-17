package com.campusconnect.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Entity
@Table(name = "semesters")
public class Semester {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull(message = "Semester number is required")
    @Min(value = 1, message = "Semester must be at least 1")
    @Max(value = 8, message = "Semester cannot be greater than 8")
    @Column(nullable = false, unique = true)
    private Integer number;

    public Semester() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }
}