package com.campusconnect.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "campus_events")
public class CampusEvent {

@Id
@GeneratedValue(strategy = GenerationType.UUID)
private UUID id;

@Column(nullable = false, length = 150)
private String title;

@Column(nullable = false, length = 5000)
private String description;

@Column(nullable = false, length = 80)
private String category;

@Column(nullable = false, length = 200)
private String venue;

@Column(name = "start_date_time", nullable = false)
private LocalDateTime startDateTime;

@Column(name = "end_date_time", nullable = false)
private LocalDateTime endDateTime;

@Column(name = "registration_deadline", nullable = false)
private LocalDateTime registrationDeadline;

@Column
private Integer capacity;

@Column(nullable = false)
private boolean published = false;

@ManyToOne(fetch = FetchType.LAZY, optional = false)
@JoinColumn(name = "created_by", nullable = false)
private User createdBy;

public CampusEvent() {
}

public UUID getId() {
    return id;
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

public boolean isPublished() {
    return published;
}

public void setPublished(boolean published) {
    this.published = published;
}

public User getCreatedBy() {
    return createdBy;
}

public void setCreatedBy(User createdBy) {
    this.createdBy = createdBy;
}
}
