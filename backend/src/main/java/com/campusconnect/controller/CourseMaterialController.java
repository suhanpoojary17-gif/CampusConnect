package com.campusconnect.controller;

import com.campusconnect.dto.CourseMaterialRequest;
import com.campusconnect.dto.CourseMaterialResponse;
import com.campusconnect.service.CourseMaterialService;

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
@RequestMapping("/api/course-materials")
public class CourseMaterialController {

    private final CourseMaterialService courseMaterialService;

    public CourseMaterialController(
            CourseMaterialService courseMaterialService
    ) {
        this.courseMaterialService =
                courseMaterialService;
    }

    // ---------------------------------------------------------
    // CREATE
    // ---------------------------------------------------------

    @PostMapping
    public ResponseEntity<CourseMaterialResponse> createMaterial(
            @Valid @RequestBody CourseMaterialRequest request,
            Authentication authentication
    ) {

        String teacherEmail =
                authentication.getName();

        return ResponseEntity.ok(
                courseMaterialService.createMaterial(
                        request,
                        teacherEmail
                )
        );
    }

    // ---------------------------------------------------------
    // GET ALL
    // ---------------------------------------------------------

    @GetMapping
    public ResponseEntity<List<CourseMaterialResponse>> getMaterials(
            Authentication authentication
    ) {

        String userEmail =
                authentication.getName();

        return ResponseEntity.ok(
                courseMaterialService.getMaterials(
                        userEmail
                )
        );
    }

    // ---------------------------------------------------------
    // GET BY ID
    // ---------------------------------------------------------

    @GetMapping("/{id}")
    public ResponseEntity<CourseMaterialResponse> getMaterialById(
            @PathVariable UUID id,
            Authentication authentication
    ) {

        String userEmail =
                authentication.getName();

        return ResponseEntity.ok(
                courseMaterialService.getMaterialById(
                        id,
                        userEmail
                )
        );
    }

    // ---------------------------------------------------------
    // UPDATE
    // ---------------------------------------------------------

    @PutMapping("/{id}")
    public ResponseEntity<CourseMaterialResponse> updateMaterial(
            @PathVariable UUID id,
            @Valid @RequestBody CourseMaterialRequest request,
            Authentication authentication
    ) {

        String userEmail =
                authentication.getName();

        return ResponseEntity.ok(
                courseMaterialService.updateMaterial(
                        id,
                        request,
                        userEmail
                )
        );
    }

    // ---------------------------------------------------------
    // DELETE
    // ---------------------------------------------------------

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteMaterial(
            @PathVariable UUID id,
            Authentication authentication
    ) {

        String userEmail =
                authentication.getName();

        courseMaterialService.deleteMaterial(
                id,
                userEmail
        );

        return ResponseEntity.ok(
                "Course material deleted successfully"
        );
    }

    // ---------------------------------------------------------
    // UPLOAD ATTACHMENT
    // ---------------------------------------------------------

    @PostMapping(
            value = "/{id}/attachment",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<String> uploadAttachment(
            @PathVariable UUID id,
            @RequestParam("file") MultipartFile file,
            Authentication authentication
    ) {

        String userEmail =
                authentication.getName();

        courseMaterialService.uploadAttachment(
                id,
                file,
                userEmail
        );

        return ResponseEntity.ok(
                "Course material attachment uploaded successfully"
        );
    }

    // ---------------------------------------------------------
    // DOWNLOAD ATTACHMENT
    // ---------------------------------------------------------

    @GetMapping("/{id}/attachment")
    public ResponseEntity<Resource> downloadAttachment(
            @PathVariable UUID id,
            Authentication authentication
    ) {

        String userEmail =
                authentication.getName();

        Resource resource =
                courseMaterialService.downloadAttachment(
                        id,
                        userEmail
                );

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" +
                                resource.getFilename() +
                                "\""
                )
                .body(resource);
    }
}