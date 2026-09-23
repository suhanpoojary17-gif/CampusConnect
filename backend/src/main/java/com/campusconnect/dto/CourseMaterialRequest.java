package com.campusconnect.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class CourseMaterialRequest {

    @NotBlank(message = "Material title is required")
    private String title;

    @NotBlank(message = "Material description is required")
    private String description;

    @NotNull(message = "Section is required")
    private UUID sectionId;

    @NotNull(message = "Subject is required")
    private UUID subjectId;

    public CourseMaterialRequest() {
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