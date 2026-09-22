package com.campusconnect.controller;

import com.campusconnect.dto.ChatbotRequest;
import com.campusconnect.dto.ChatbotResponse;
import com.campusconnect.service.ChatbotService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chatbot")
public class ChatbotController {

    private final ChatbotService chatbotService;

    public ChatbotController(ChatbotService chatbotService) {
        this.chatbotService = chatbotService;
    }

    @PostMapping
    public ResponseEntity<ChatbotResponse> chat(
            @Valid @RequestBody ChatbotRequest request,
            Authentication authentication) {

        String userEmail = authentication.getName();

        String response = chatbotService.processMessage(
                request.getMessage(),
                userEmail
        );

        return ResponseEntity.ok(
                new ChatbotResponse(response)
        );
    }
}