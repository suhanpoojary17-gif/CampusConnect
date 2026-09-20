package com.campusconnect.controller;

import com.campusconnect.dto.FeedbackRequest;
import com.campusconnect.dto.FeedbackResponse;
import com.campusconnect.entity.User;
import com.campusconnect.repository.UserRepository;
import com.campusconnect.service.FeedbackService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {

    private final FeedbackService feedbackService;
    private final UserRepository userRepository;

    public FeedbackController(FeedbackService feedbackService,
                              UserRepository userRepository) {
        this.feedbackService = feedbackService;
        this.userRepository = userRepository;
    }

    // Student submits feedback
    @PostMapping
    public ResponseEntity<FeedbackResponse> submitFeedback(
            @RequestBody FeedbackRequest request,
            Authentication authentication) {

        String email = authentication.getName();

        User student = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("Student not found"));

        FeedbackResponse response =
                feedbackService.submitFeedback(
                        student.getId(),
                        request
                );

        return ResponseEntity.ok(response);
    }

    // Student views their own feedback
    @GetMapping("/my")
    public ResponseEntity<List<FeedbackResponse>> getMyFeedback(
            Authentication authentication) {

        String email = authentication.getName();

        User student = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("Student not found"));

        List<FeedbackResponse> feedback =
                feedbackService.getStudentFeedback(
                        student.getId()
                );

        return ResponseEntity.ok(feedback);
    }
}