package com.campusconnect.controller;

import com.campusconnect.dto.ExamRequest;
import com.campusconnect.dto.ExamResponse;
import com.campusconnect.service.ExamService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.campusconnect.dto.BulkExamRequest;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/exams")
public class ExamController {

    private final ExamService examService;

    public ExamController(ExamService examService) {
        this.examService = examService;
    }

    @PostMapping
    public ResponseEntity<ExamResponse> createExam(
            @Valid @RequestBody ExamRequest request
    ) {
        return ResponseEntity.ok(
                examService.createExam(request)
        );
    }

    @GetMapping
    public ResponseEntity<List<ExamResponse>> getAllExams() {

        return ResponseEntity.ok(
                examService.getAllExams()
        );
    }

    @GetMapping("/{id:[0-9a-fA-F-]{36}}")
    public ResponseEntity<ExamResponse> getExam(
            @PathVariable UUID id
    ) {

        return ResponseEntity.ok(
                examService.getExamById(id)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExamResponse> updateExam(
            @PathVariable UUID id,
            @Valid @RequestBody ExamRequest request
    ) {

        return ResponseEntity.ok(
                examService.updateExam(id, request)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExam(
            @PathVariable UUID id
    ) {

        examService.deleteExam(id);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/bulk")
    public ResponseEntity<List<ExamResponse>> createBulkExams(
            @Valid @RequestBody BulkExamRequest request
    ) {

        return ResponseEntity.ok(
                examService.createBulkExams(
                        request.getExams()
                )
        );
    }
}