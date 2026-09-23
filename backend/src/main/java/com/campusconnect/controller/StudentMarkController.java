package com.campusconnect.controller;

import com.campusconnect.dto.StudentMarkRequest;
import com.campusconnect.dto.StudentMarkResponse;
import com.campusconnect.service.StudentMarkService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class StudentMarkController {

    private final StudentMarkService studentMarkService;

    public StudentMarkController(
            StudentMarkService studentMarkService
    ) {
        this.studentMarkService = studentMarkService;
    }

    @PostMapping("/assessments/{assessmentId}/marks")
    public ResponseEntity<StudentMarkResponse> saveMark(
            @PathVariable UUID assessmentId,
            @Valid @RequestBody StudentMarkRequest request,
            Authentication authentication
    ) {

        String email = authentication.getName();

        StudentMarkResponse response =
                studentMarkService.saveMark(
                        assessmentId,
                        request,
                        email
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/assessments/{assessmentId}/marks")
    public ResponseEntity<List<StudentMarkResponse>> getMarksForAssessment(
            @PathVariable UUID assessmentId,
            Authentication authentication
    ) {

        String email = authentication.getName();

        return ResponseEntity.ok(
                studentMarkService.getMarksForAssessment(
                        assessmentId,
                        email
                )
        );
    }

    @GetMapping("/marks/my")
    public ResponseEntity<List<StudentMarkResponse>> getMyMarks(
            Authentication authentication
    ) {

        String email = authentication.getName();

        return ResponseEntity.ok(
                studentMarkService.getMyMarks(email)
        );
    }
}