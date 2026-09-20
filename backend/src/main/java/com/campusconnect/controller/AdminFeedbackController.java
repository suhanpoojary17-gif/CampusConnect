package com.campusconnect.controller;

import com.campusconnect.dto.FeedbackResponse;
import com.campusconnect.service.FeedbackService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/feedback")
public class AdminFeedbackController {

    private final FeedbackService feedbackService;

    public AdminFeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    // Admin views all feedback
    @GetMapping
    public ResponseEntity<List<FeedbackResponse>> getAllFeedback() {

        List<FeedbackResponse> feedback =
                feedbackService.getAllFeedback();

        return ResponseEntity.ok(feedback);
    }

    // Admin deletes feedback
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteFeedback(
            @PathVariable UUID id) {

        feedbackService.deleteFeedback(id);

        return ResponseEntity.ok(
                "Feedback deleted successfully"
        );
    }
}