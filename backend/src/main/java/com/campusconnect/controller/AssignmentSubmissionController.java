package com.campusconnect.controller;

import com.campusconnect.dto.AssignmentSubmissionRequest;
import com.campusconnect.dto.AssignmentSubmissionResponse;
import com.campusconnect.model.AssignmentSubmission;
import com.campusconnect.service.AssignmentSubmissionFileService;
import com.campusconnect.service.AssignmentSubmissionService;
import jakarta.validation.Valid;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class AssignmentSubmissionController {

    private final AssignmentSubmissionService submissionService;
    private final AssignmentSubmissionFileService fileService;

    public AssignmentSubmissionController(
            AssignmentSubmissionService submissionService,
            AssignmentSubmissionFileService fileService
    ) {
        this.submissionService = submissionService;
        this.fileService = fileService;
    }

    // ==========================================
    // STUDENT - SUBMIT / RESUBMIT ASSIGNMENT
    // ==========================================

    @PostMapping(
            value = "/assignments/{assignmentId}/submissions",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<AssignmentSubmissionResponse> submitAssignment(
            @PathVariable UUID assignmentId,

            @RequestPart(value = "submission", required = false)
            @Valid AssignmentSubmissionRequest request,

            @RequestPart("file")
            MultipartFile file,

            Authentication authentication
    ) {

        String studentEmail =
                authentication.getName();

        AssignmentSubmissionResponse response =
                submissionService.submitAssignment(
                        assignmentId,
                        request,
                        file,
                        studentEmail
                );

        return ResponseEntity.ok(response);
    }

    // ==========================================
    // STUDENT - VIEW OWN SUBMISSION
    // ==========================================

    @GetMapping(
            "/assignments/{assignmentId}/submissions/my"
    )
    public ResponseEntity<AssignmentSubmissionResponse>
    getMySubmission(
            @PathVariable UUID assignmentId,
            Authentication authentication
    ) {

        String studentEmail =
                authentication.getName();

        return ResponseEntity.ok(
                submissionService.getMySubmission(
                        assignmentId,
                        studentEmail
                )
        );
    }

    // ==========================================
    // TEACHER / ADMIN - VIEW ALL SUBMISSIONS
    // ==========================================

    @GetMapping(
            "/assignments/{assignmentId}/submissions"
    )
    public ResponseEntity<List<AssignmentSubmissionResponse>>
    getSubmissions(
            @PathVariable UUID assignmentId,
            Authentication authentication
    ) {

        String userEmail =
                authentication.getName();

        return ResponseEntity.ok(
                submissionService.getSubmissions(
                        assignmentId,
                        userEmail
                )
        );
    }

    // ==========================================
    // STUDENT / TEACHER / ADMIN
    // VIEW SINGLE SUBMISSION
    // ==========================================

    @GetMapping(
            "/submissions/{submissionId}"
    )
    public ResponseEntity<AssignmentSubmissionResponse>
    getSubmission(
            @PathVariable UUID submissionId,
            Authentication authentication
    ) {

        String userEmail =
                authentication.getName();

        return ResponseEntity.ok(
                submissionService.getSubmission(
                        submissionId,
                        userEmail
                )
        );
    }

    // ==========================================
    // STUDENT / TEACHER / ADMIN
    // DOWNLOAD SUBMISSION FILE
    // ==========================================

    @GetMapping(
            "/submissions/{submissionId}/attachment"
    )
    public ResponseEntity<ByteArrayResource>
    downloadAttachment(
            @PathVariable UUID submissionId,
            Authentication authentication
    ) {

        String userEmail =
                authentication.getName();

        AssignmentSubmission submission =
                submissionService
                        .getSubmissionEntityForDownload(
                                submissionId,
                                userEmail
                        );

        byte[] fileData =
                fileService.getFile(
                        submission.getAttachmentPath()
                );

        ByteArrayResource resource =
                new ByteArrayResource(fileData);

        String fileName = "submission-file";

        if (submission.getAttachmentPath() != null) {

            String path =
                    submission.getAttachmentPath();

            int lastSlash =
                    Math.max(
                            path.lastIndexOf('/'),
                            path.lastIndexOf('\\')
                    );

            if (lastSlash >= 0 &&
                    lastSlash < path.length() - 1) {

                fileName =
                        path.substring(lastSlash + 1);
            }
        }

        return ResponseEntity.ok()
                .contentType(
                        MediaType.APPLICATION_OCTET_STREAM
                )
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" +
                                fileName +
                                "\""
                )
                .body(resource);
    }
}