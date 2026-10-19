package com.campusconnect.dto;

import com.campusconnect.entity.EventRegistrationStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public class EventRegistrationResponse {

    private UUID id;
    private UUID eventId;
    private String eventTitle;
    private String venue;
    private LocalDateTime eventStartDateTime;
    private LocalDateTime eventEndDateTime;
    private LocalDateTime registeredAt;
    private EventRegistrationStatus status;
    private Long studentId;
    private String studentEmail;

    public EventRegistrationResponse() {
    }

    public EventRegistrationResponse(
            UUID id,
            UUID eventId,
            String eventTitle,
            String venue,
            LocalDateTime eventStartDateTime,
            LocalDateTime eventEndDateTime,
            LocalDateTime registeredAt,
            EventRegistrationStatus status,
            Long studentId,
            String studentEmail
    ) {
        this.id = id;
        this.eventId = eventId;
        this.eventTitle = eventTitle;
        this.venue = venue;
        this.eventStartDateTime = eventStartDateTime;
        this.eventEndDateTime = eventEndDateTime;
        this.registeredAt = registeredAt;
        this.status = status;
        this.studentId = studentId;
        this.studentEmail = studentEmail;
    }

    public UUID getId() {
        return id;
    }

    public UUID getEventId() {
        return eventId;
    }

    public String getEventTitle() {
        return eventTitle;
    }

    public String getVenue() {
        return venue;
    }

    public LocalDateTime getEventStartDateTime() {
        return eventStartDateTime;
    }

    public LocalDateTime getEventEndDateTime() {
        return eventEndDateTime;
    }

    public LocalDateTime getRegisteredAt() {
        return registeredAt;
    }

    public EventRegistrationStatus getStatus() {
        return status;
    }

    public Long getStudentId() {
        return studentId;
    }

    public String getStudentEmail() {
        return studentEmail;
    }
}