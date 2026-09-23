package com.campusconnect.controller;

import com.campusconnect.dto.AssignmentRequest;
import com.campusconnect.dto.AssignmentResponse;
import com.campusconnect.service.AssignmentService;

import jakarta.validation.Valid;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/assignments")
public class AssignmentController {

    private final AssignmentService assignmentService;

    public AssignmentController(
            AssignmentService assignmentService
    ) {
        this.assignmentService = assignmentService;
    }

    // Create assignment
    @PostMapping
    public ResponseEntity<AssignmentResponse> createAssignment(
            @Valid @RequestBody AssignmentRequest request,
            Authentication authentication
    ) {

        String teacherEmail =
                authentication.getName();

        AssignmentResponse response =
                assignmentService.createAssignment(
                        request,
                        teacherEmail
                );

        return ResponseEntity.ok(response);
    }

    // Get assignments visible to logged-in user
    @GetMapping
    public ResponseEntity<List<AssignmentResponse>> getAssignments(
            Authentication authentication
    ) {

        String userEmail =
                authentication.getName();

        return ResponseEntity.ok(
                assignmentService.getAssignments(
                        userEmail
                )
        );
    }

    // Get assignment by ID
    @GetMapping("/{id}")
    public ResponseEntity<AssignmentResponse> getAssignmentById(
            @PathVariable UUID id,
            Authentication authentication
    ) {

        String userEmail =
                authentication.getName();

        return ResponseEntity.ok(
                assignmentService.getAssignmentById(
                        id,
                        userEmail
                )
        );
    }

    // Update assignment
    @PutMapping("/{id}")
    public ResponseEntity<AssignmentResponse> updateAssignment(
            @PathVariable UUID id,
            @Valid @RequestBody AssignmentRequest request,
            Authentication authentication
    ) {

        String userEmail =
                authentication.getName();

        return ResponseEntity.ok(
                assignmentService.updateAssignment(
                        id,
                        request,
                        userEmail
                )
        );
    }

    // Delete assignment
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteAssignment(
            @PathVariable UUID id,
            Authentication authentication
    ) {

        String userEmail =
                authentication.getName();

        assignmentService.deleteAssignment(
                id,
                userEmail
        );

        return ResponseEntity.ok(
                "Assignment deleted successfully"
        );
    }

    // Upload assignment attachment
    @PostMapping(
            value = "/{id}/attachment",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<String> uploadAttachment(
            @PathVariable UUID id,
            @RequestParam("file") MultipartFile file,
            Authentication authentication
    ) {

        String userEmail =
                authentication.getName();

        assignmentService.uploadAttachment(
                id,
                file,
                userEmail
        );

        return ResponseEntity.ok(
                "Assignment attachment uploaded successfully"
        );
    }

    // Download assignment attachment
    @GetMapping("/{id}/attachment")
    public ResponseEntity<Resource> downloadAttachment(
            @PathVariable UUID id,
            Authentication authentication
    ) {

        String userEmail =
                authentication.getName();

        Resource resource =
                assignmentService.downloadAttachment(
                        id,
                        userEmail
                );

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" +
                                resource.getFilename() +
                                "\""
                )
                .body(resource);
    }
}