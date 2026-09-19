package com.campusconnect.controller;

import com.campusconnect.dto.NoticeRequest;
import com.campusconnect.dto.NoticeResponse;
import com.campusconnect.service.NoticeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notices")
public class NoticeController {

    private final NoticeService noticeService;

    public NoticeController(NoticeService noticeService) {
        this.noticeService = noticeService;
    }

    @PostMapping
    public ResponseEntity<NoticeResponse> createNotice(
            @Valid @RequestBody NoticeRequest request,
            Authentication authentication
    ) {

        String creatorEmail = authentication.getName();

        NoticeResponse response =
                noticeService.createNotice(request, creatorEmail, null);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<NoticeResponse>> getAllNotices(
            Authentication authentication
    ) {
        String userEmail = authentication.getName();

        return ResponseEntity.ok(
                noticeService.getAllNotices(userEmail)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<NoticeResponse> updateNotice(
            @PathVariable UUID id,
            @Valid @RequestBody NoticeRequest request,
            Authentication authentication
    ) {
        String creatorEmail = authentication.getName();

        return ResponseEntity.ok(
                noticeService.updateNotice(id, request, creatorEmail)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotice(
            @PathVariable UUID id,
            Authentication authentication
    ) {
        String creatorEmail = authentication.getName();
        noticeService.deleteNotice(id, creatorEmail);
        return ResponseEntity.noContent().build();
    }
}