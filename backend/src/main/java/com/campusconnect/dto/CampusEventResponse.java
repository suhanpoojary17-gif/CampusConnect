package com.campusconnect.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class CampusEventResponse {

    private UUID id;
    private String title;
    private String description;
    private String category;
    private String venue;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private LocalDateTime registrationDeadline;
    private Integer capacity;
    private boolean published;
    private long registrationCount;
    private String createdByEmail;

    public CampusEventResponse() {
    }

    public CampusEventResponse(
            UUID id,
            String title,
            String description,
            String category,
            String venue,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime,
            LocalDateTime registrationDeadline,
            Integer capacity,
            boolean published,
            long registrationCount,
            String createdByEmail
    ) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.venue = venue;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.registrationDeadline = registrationDeadline;
        this.capacity = capacity;
        this.published = published;
        this.registrationCount = registrationCount;
        this.createdByEmail = createdByEmail;
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

    public String getCategory() {
        return category;
    }

    public String getVenue() {
        return venue;
    }

    public LocalDateTime getStartDateTime() {
        return startDateTime;
    }

    public LocalDateTime getEndDateTime() {
        return endDateTime;
    }

    public LocalDateTime getRegistrationDeadline() {
        return registrationDeadline;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public boolean isPublished() {
        return published;
    }

    public long getRegistrationCount() {
        return registrationCount;
    }

    public String getCreatedByEmail() {
        return createdByEmail;
    }
}