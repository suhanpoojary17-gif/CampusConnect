package com.campusconnect.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class CampusEventRequest {

    @NotBlank(message = "Event title is required")
    @Size(max = 150, message = "Event title cannot exceed 150 characters")
    private String title;

    @NotBlank(message = "Event description is required")
    @Size(max = 5000, message = "Description cannot exceed 5000 characters")
    private String description;

    @NotBlank(message = "Event category is required")
    @Size(max = 80, message = "Category cannot exceed 80 characters")
    private String category;

    @NotBlank(message = "Event venue is required")
    @Size(max = 200, message = "Venue cannot exceed 200 characters")
    private String venue;

    @NotNull(message = "Event start date and time are required")
    private LocalDateTime startDateTime;

    @NotNull(message = "Event end date and time are required")
    private LocalDateTime endDateTime;

    @NotNull(message = "Registration deadline is required")
    private LocalDateTime registrationDeadline;

    @Min(value = 1, message = "Capacity must be at least 1")
    private Integer capacity;

    public CampusEventRequest() {
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public LocalDateTime getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(LocalDateTime startDateTime) {
        this.startDateTime = startDateTime;
    }

    public LocalDateTime getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(LocalDateTime endDateTime) {
        this.endDateTime = endDateTime;
    }

    public LocalDateTime getRegistrationDeadline() {
        return registrationDeadline;
    }

    public void setRegistrationDeadline(LocalDateTime registrationDeadline) {
        this.registrationDeadline = registrationDeadline;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }
}