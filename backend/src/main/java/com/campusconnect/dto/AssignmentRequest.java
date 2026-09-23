package com.campusconnect.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;
import java.util.UUID;

public class AssignmentRequest {

    @NotBlank(message = "Assignment title is required")
    private String title;

    @NotBlank(message = "Assignment description is required")
    private String description;

    @NotNull(message = "Due date and time are required")
    private LocalDateTime dueDateTime;

    @NotNull(message = "Maximum marks are required")
    @Positive(message = "Maximum marks must be greater than 0")
    private Double maximumMarks;

    @NotNull(message = "Section is required")
    private UUID sectionId;

    @NotNull(message = "Subject is required")
    private UUID subjectId;

    public AssignmentRequest() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getDueDateTime() {
        return dueDateTime;
    }

    public void setDueDateTime(LocalDateTime dueDateTime) {
        this.dueDateTime = dueDateTime;
    }

    public Double getMaximumMarks() {
        return maximumMarks;
    }

    public void setMaximumMarks(Double maximumMarks) {
        this.maximumMarks = maximumMarks;
    }

    public UUID getSectionId() {
        return sectionId;
    }

    public void setSectionId(UUID sectionId) {
        this.sectionId = sectionId;
    }

    public UUID getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(UUID subjectId) {
        this.subjectId = subjectId;
    }
}