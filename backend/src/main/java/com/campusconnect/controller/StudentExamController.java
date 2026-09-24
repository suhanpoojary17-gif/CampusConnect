package com.campusconnect.controller;

import com.campusconnect.dto.ExamResponse;
import com.campusconnect.service.StudentExamService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/exams")
public class StudentExamController {

    private final StudentExamService studentExamService;

    public StudentExamController(
            StudentExamService studentExamService
    ) {
        this.studentExamService = studentExamService;
    }

    @GetMapping("/my")
    public ResponseEntity<List<ExamResponse>> getMyExamTimetable(
            Authentication authentication
    ) {
        String email = authentication.getName();

        return ResponseEntity.ok(
                studentExamService.getMyExamTimetable(email)
        );
    }
}