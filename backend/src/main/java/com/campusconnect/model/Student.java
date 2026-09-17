package com.campusconnect.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Entity
@Table(name = "students")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank(message = "Student name is required")
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "Student email is required")
    @Email(message = "Enter a valid email address")
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank(message = "Student password is required")
    @Column(nullable = false)
    private String password;

    @NotNull(message = "Student section is required")
    @ManyToOne
    @JoinColumn(name = "section_id", nullable = false)
    private Section section;

    public Student() {
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Section getSection() {
        return section;
    }

    public void setSection(Section section) {
        this.section = section;
    }
}