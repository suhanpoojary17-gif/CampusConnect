package com.campusconnect.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Entity
@Table(name = "subjects")
public class Subject {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank(message = "Subject name is required")
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "Subject code is required")
    @Column(nullable = false, unique = true)
    private String code;

    @NotNull(message = "Section is required")
    @ManyToOne
    @JoinColumn(name = "section_id", nullable = false)
    private Section section;

    public Subject() {
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

    public Section getSection() {
        return section;
    }

    public void setSection(Section section) {
        this.section = section;
    }
}