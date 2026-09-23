package com.campusconnect.controller;

import com.campusconnect.dto.AssessmentRequest;
import com.campusconnect.dto.AssessmentResponse;
import com.campusconnect.service.AssessmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/assessments")
public class AssessmentController {

    private final AssessmentService assessmentService;

    public AssessmentController(AssessmentService assessmentService) {
        this.assessmentService = assessmentService;
    }

    @PostMapping
    public ResponseEntity<AssessmentResponse> createAssessment(
            @Valid @RequestBody AssessmentRequest request,
            Authentication authentication
    ) {

        String email = authentication.getName();

        AssessmentResponse response =
                assessmentService.createAssessment(
                        request,
                        email
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<AssessmentResponse>> getAllAssessments(
            Authentication authentication
    ) {

        String email = authentication.getName();

        return ResponseEntity.ok(
                assessmentService.getAllAssessments(email)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<AssessmentResponse> getAssessmentById(
            @PathVariable UUID id
    ) {

        return ResponseEntity.ok(
                assessmentService.getAssessmentById(id)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<AssessmentResponse> updateAssessment(
            @PathVariable UUID id,
            @Valid @RequestBody AssessmentRequest request,
            Authentication authentication
    ) {

        String email = authentication.getName();

        return ResponseEntity.ok(
                assessmentService.updateAssessment(
                        id,
                        request,
                        email
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAssessment(
            @PathVariable UUID id,
            Authentication authentication
    ) {

        String email = authentication.getName();

        assessmentService.deleteAssessment(
                id,
                email
        );

        return ResponseEntity.noContent().build();
    }
}