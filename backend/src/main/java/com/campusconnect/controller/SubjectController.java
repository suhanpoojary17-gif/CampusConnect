package com.campusconnect.controller;

import com.campusconnect.model.Subject;
import com.campusconnect.service.SubjectService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/subjects")
public class SubjectController {

    private final SubjectService subjectService;

    public SubjectController(SubjectService subjectService) {
        this.subjectService = subjectService;
    }

    @PostMapping
    public ResponseEntity<Subject> createSubject(
            @Valid @RequestBody Subject subject) {

        return ResponseEntity.ok(
                subjectService.createSubject(subject)
        );
    }

    @GetMapping
    public ResponseEntity<List<Subject>> getAllSubjects() {

        return ResponseEntity.ok(
                subjectService.getAllSubjects()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Subject> getSubjectById(
            @PathVariable UUID id) {

        return ResponseEntity.ok(
                subjectService.getSubjectById(id)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<Subject> updateSubject(
            @PathVariable UUID id,
            @Valid @RequestBody Subject subject) {

        return ResponseEntity.ok(
                subjectService.updateSubject(id, subject)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSubject(
            @PathVariable UUID id) {

        subjectService.deleteSubject(id);

        return ResponseEntity.noContent().build();
    }
}