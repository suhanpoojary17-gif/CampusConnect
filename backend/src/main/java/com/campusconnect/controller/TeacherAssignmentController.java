package com.campusconnect.controller;

import com.campusconnect.entity.TeacherAssignment;
import com.campusconnect.service.TeacherAssignmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/teacher-assignments")
public class TeacherAssignmentController {

    private final TeacherAssignmentService assignmentService;

    public TeacherAssignmentController(
            TeacherAssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    @PostMapping
    public ResponseEntity<TeacherAssignment> createAssignment(
            @RequestParam Long teacherId,
            @RequestParam UUID sectionId,
            @RequestParam UUID subjectId) {

        TeacherAssignment assignment =
                assignmentService.createAssignment(
                        teacherId,
                        sectionId,
                        subjectId
                );

        return ResponseEntity.ok(assignment);
    }
}