package com.campusconnect.controller;

import com.campusconnect.service.TeacherAssignmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/teacher")
public class TeacherAssignmentTestController {

    private final TeacherAssignmentService assignmentService;

    public TeacherAssignmentTestController(
            TeacherAssignmentService assignmentService) {

        this.assignmentService = assignmentService;
    }

    @GetMapping("/access")
    public ResponseEntity<String> checkAccess(
            @RequestParam UUID sectionId,
            @RequestParam UUID subjectId,
            Authentication authentication) {

        boolean assigned =
                assignmentService.isTeacherAssigned(
                        authentication.getName(),
                        sectionId,
                        subjectId
                );

        if (!assigned) {
            return ResponseEntity
                    .status(403)
                    .body("You are not assigned to this section and subject");
        }

        return ResponseEntity.ok(
                "Teacher is authorized for this section and subject"
        );
    }
}