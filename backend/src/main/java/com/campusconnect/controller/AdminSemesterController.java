package com.campusconnect.controller;

import com.campusconnect.dto.SemesterRequest;
import com.campusconnect.model.Semester;
import com.campusconnect.repository.SemesterRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin/semesters")
public class AdminSemesterController {

    private final SemesterRepository semesterRepository;

    public AdminSemesterController(
            SemesterRepository semesterRepository) {
        this.semesterRepository = semesterRepository;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Semester> createSemester(
            @Valid @RequestBody SemesterRequest request) {

        Semester semester = new Semester();
        semester.setNumber(request.getNumber());

        return ResponseEntity.ok(
                semesterRepository.save(semester)
        );
    }
}