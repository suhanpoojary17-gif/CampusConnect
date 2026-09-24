package com.campusconnect.controller;

import com.campusconnect.dto.StudentExamResultResponse;
import com.campusconnect.service.StudentExamResultService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/results")
public class StudentExamResultController {

    private final StudentExamResultService studentExamResultService;

    public StudentExamResultController(
            StudentExamResultService studentExamResultService
    ) {
        this.studentExamResultService =
                studentExamResultService;
    }

    @GetMapping("/my")
    public ResponseEntity<List<StudentExamResultResponse>> getMyResults(
            Authentication authentication
    ) {

        String email = authentication.getName();

        return ResponseEntity.ok(
                studentExamResultService.getMyResults(email)
        );
    }
}