package com.campusconnect.controller;

import com.campusconnect.dto.BulkExamMarksRequest;
import com.campusconnect.dto.ExamMarkResponse;
import com.campusconnect.dto.ExamResponse;
import com.campusconnect.service.TeacherExamService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/exams/teacher")
public class TeacherExamController {

    private final TeacherExamService teacherExamService;

    public TeacherExamController(
            TeacherExamService teacherExamService
    ) {
        this.teacherExamService = teacherExamService;
    }

    @GetMapping
    public ResponseEntity<List<ExamResponse>> getMyEligibleExams(
            Authentication authentication
    ) {

        String email = authentication.getName();

        return ResponseEntity.ok(
                teacherExamService.getMyEligibleExams(email)
        );
    }

    @PostMapping("/{examId}/marks")
    public ResponseEntity<List<ExamMarkResponse>> enterBulkMarks(
            @PathVariable UUID examId,
            @Valid @RequestBody BulkExamMarksRequest request,
            Authentication authentication
    ) {

        String email = authentication.getName();

        return ResponseEntity.ok(
                teacherExamService.enterBulkMarks(
                        examId,
                        request.getMarks(),
                        email
                )
        );
    }

    @GetMapping("/{examId}/marks")
    public ResponseEntity<List<ExamMarkResponse>> getExamMarks(
            @PathVariable UUID examId,
            Authentication authentication
    ) {

        String email = authentication.getName();

        return ResponseEntity.ok(
                teacherExamService.getExamMarks(
                        examId,
                        email
                )
        );
    }
}