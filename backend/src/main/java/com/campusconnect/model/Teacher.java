package com.campusconnect.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

@Entity
@Table(name = "teachers")
public class Teacher {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank(message = "Teacher name is required")
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "Teacher email is required")
    @Email(message = "Enter a valid email address")
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank(message = "Teacher password is required")
    @Column(nullable = false)
    private String password;

    public Teacher() {
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
}