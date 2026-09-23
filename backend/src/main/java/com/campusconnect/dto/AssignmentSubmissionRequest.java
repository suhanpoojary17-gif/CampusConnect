package com.campusconnect.dto;

import jakarta.validation.constraints.Size;

public class AssignmentSubmissionRequest {

    @Size(max = 1000, message = "Comment cannot exceed 1000 characters")
    private String comment;

    public AssignmentSubmissionRequest() {
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}