package com.campusconnect.controller;

import com.campusconnect.entity.Notification;
import com.campusconnect.entity.User;
import com.campusconnect.repository.UserRepository;
import com.campusconnect.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    public NotificationController(NotificationService notificationService,
                                   UserRepository userRepository) {
        this.notificationService = notificationService;
        this.userRepository = userRepository;
    }

    @GetMapping("/my")
    public ResponseEntity<List<Notification>> getMyNotifications(
            Authentication authentication) {

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(
                notificationService.getUserNotifications(user.getId())
        );
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount(
            Authentication authentication) {

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(
                notificationService.getUnreadCount(user.getId())
        );
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Notification> markAsRead(
            @PathVariable UUID id,
            Authentication authentication) {

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(
                notificationService.markAsRead(id, user.getId())
        );
    }
}