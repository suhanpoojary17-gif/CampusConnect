package com.campusconnect.dto;

import java.time.LocalDate;
import java.util.UUID;

public class NoticeResponse {

    private UUID id;
    private String title;
    private String description;
    private LocalDate date;
    private UUID targetSectionId;
    private String targetSectionName;
    private Long creatorId;
    private String creatorEmail;
    private String attachmentPath;

    public NoticeResponse() {
    }

    public NoticeResponse(
            UUID id,
            String title,
            String description,
            LocalDate date,
            UUID targetSectionId,
            String targetSectionName,
            Long creatorId,
            String creatorEmail,
            String attachmentPath
    ) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.date = date;
        this.targetSectionId = targetSectionId;
        this.targetSectionName = targetSectionName;
        this.creatorId = creatorId;
        this.creatorEmail = creatorEmail;
        this.attachmentPath = attachmentPath;
    }

    public UUID getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public LocalDate getDate() {
        return date;
    }

    public UUID getTargetSectionId() {
        return targetSectionId;
    }

    public String getTargetSectionName() {
        return targetSectionName;
    }

    public Long getCreatorId() {
        return creatorId;
    }

    public String getCreatorEmail() {
        return creatorEmail;
    }

    public String getAttachmentPath() {
        return attachmentPath;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setTargetSectionId(UUID targetSectionId) {
        this.targetSectionId = targetSectionId;
    }

    public void setTargetSectionName(String targetSectionName) {
        this.targetSectionName = targetSectionName;
    }

    public void setCreatorId(Long creatorId) {
        this.creatorId = creatorId;
    }

    public void setCreatorEmail(String creatorEmail) {
        this.creatorEmail = creatorEmail;
    }

    public void setAttachmentPath(String attachmentPath) {
        this.attachmentPath = attachmentPath;
    }
}