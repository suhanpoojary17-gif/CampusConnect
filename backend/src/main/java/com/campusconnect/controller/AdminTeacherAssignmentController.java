package com.campusconnect.controller;

import com.campusconnect.dto.TeacherAssignmentRequest;
import com.campusconnect.entity.TeacherAssignment;
import com.campusconnect.service.TeacherAssignmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/teacher-assignments")
public class AdminTeacherAssignmentController {

    private final TeacherAssignmentService assignmentService;

    public AdminTeacherAssignmentController(
            TeacherAssignmentService assignmentService) {

        this.assignmentService = assignmentService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TeacherAssignment> createAssignment(
            @RequestBody TeacherAssignmentRequest request) {

        TeacherAssignment assignment =
                assignmentService.createAssignment(
                        request.getTeacherId(),
                        request.getSectionId(),
                        request.getSubjectId());

        return ResponseEntity.ok(assignment);
    }
}