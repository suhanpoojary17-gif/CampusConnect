package com.campusconnect.controller;

import com.campusconnect.model.Semester;
import com.campusconnect.service.SemesterService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/semesters")
public class SemesterController {

    private final SemesterService semesterService;

    public SemesterController(SemesterService semesterService) {
        this.semesterService = semesterService;
    }

    @PostMapping
    public ResponseEntity<Semester> createSemester(
            @RequestBody Semester semester) {

        return ResponseEntity.ok(
                semesterService.createSemester(semester)
        );
    }

    @GetMapping
    public ResponseEntity<List<Semester>> getAllSemesters() {

        return ResponseEntity.ok(
                semesterService.getAllSemesters()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Semester> getSemesterById(
            @PathVariable UUID id) {

        return ResponseEntity.ok(
                semesterService.getSemesterById(id)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<Semester> updateSemester(
            @PathVariable UUID id,
            @RequestBody Semester semester) {

        return ResponseEntity.ok(
                semesterService.updateSemester(id, semester)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSemester(
            @PathVariable UUID id) {

        semesterService.deleteSemester(id);

        return ResponseEntity.noContent().build();
    }
}