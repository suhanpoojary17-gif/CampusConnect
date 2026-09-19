package com.campusconnect.controller;

import com.campusconnect.dto.NoticeRequest;
import com.campusconnect.dto.NoticeResponse;
import com.campusconnect.service.NoticeService;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    @PostMapping(
            value = "/{id}/attachment",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<NoticeResponse> uploadAttachment(
            @PathVariable UUID id,
            @RequestParam("file") MultipartFile file,
            Authentication authentication
    ) {
        String userEmail = authentication.getName();

        return ResponseEntity.ok(
                noticeService.uploadAttachment(
                        id,
                        file,
                        userEmail
                )
        );
    }

    @GetMapping("/{id}/attachment")
    public ResponseEntity<Resource> downloadAttachment(
            @PathVariable UUID id,
            Authentication authentication
    ) {
        Resource resource = noticeService.downloadAttachment(
                id,
                authentication.getName()
        );

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + resource.getFilename() + "\""
                )
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
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

    @GetMapping("/{id}")
    public ResponseEntity<NoticeResponse> getNoticeById(
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(
                noticeService.getNoticeById(id)
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