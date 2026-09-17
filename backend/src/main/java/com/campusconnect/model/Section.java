package com.campusconnect.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Entity
@Table(name = "sections")
public class Section {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank(message = "Section name is required")
    @Column(nullable = false)
    private String name;

    @NotNull(message = "Department is required")
    @ManyToOne
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @NotNull(message = "Academic year is required")
    @ManyToOne
    @JoinColumn(name = "academic_year_id", nullable = false)
    private Year year;

    @NotNull(message = "Semester is required")
    @ManyToOne
    @JoinColumn(name = "semester_id", nullable = false)
    private Semester semester;

    public Section() {
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

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public Year getYear() {
        return year;
    }

    public void setYear(Year year) {
        this.year = year;
    }

    public Semester getSemester() {
        return semester;
    }

    public void setSemester(Semester semester) {
        this.semester = semester;
    }
}