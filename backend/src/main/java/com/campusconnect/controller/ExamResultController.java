package com.campusconnect.controller;

import com.campusconnect.service.ExamResultService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/exams")
public class ExamResultController {

    private final ExamResultService examResultService;

    public ExamResultController(
            ExamResultService examResultService
    ) {
        this.examResultService = examResultService;
    }

    @PutMapping("/{examId}/submit")
    public ResponseEntity<String> submitResult(
            @PathVariable UUID examId,
            Authentication authentication
    ) {

        String email = authentication.getName();

        return ResponseEntity.ok(
                examResultService.submitResult(
                        examId,
                        email
                )
        );
    }

    @PutMapping("/{examId}/publish-result")
    public ResponseEntity<String> publishResult(
            @PathVariable UUID examId,
            Authentication authentication
    ) {

        String email = authentication.getName();

        return ResponseEntity.ok(
                examResultService.publishResult(
                        examId,
                        email
                )
        );
    }
}