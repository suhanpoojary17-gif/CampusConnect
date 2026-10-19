package com.campusconnect.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
name = "event_registrations",
uniqueConstraints = {
@UniqueConstraint(
name = "uk_event_registration_event_student",
columnNames = {"event_id", "student_id"}
)
},
indexes = {
@Index(
name = "idx_event_registration_event",
columnList = "event_id"
),
@Index(
name = "idx_event_registration_student",
columnList = "student_id"
)
}
)
public class EventRegistration {

@Id
@GeneratedValue(strategy = GenerationType.UUID)
private UUID id;

@ManyToOne(fetch = FetchType.LAZY, optional = false)
@JoinColumn(name = "event_id", nullable = false)
private CampusEvent event;

@ManyToOne(fetch = FetchType.LAZY, optional = false)
@JoinColumn(name = "student_id", nullable = false)
private User student;

@Column(name = "registered_at", nullable = false)
private LocalDateTime registeredAt;

@Enumerated(EnumType.STRING)
@Column(nullable = false, length = 20)
private EventRegistrationStatus status =
        EventRegistrationStatus.REGISTERED;

public EventRegistration() {
}

@PrePersist
protected void onCreate() {
    if (registeredAt == null) {
        registeredAt = LocalDateTime.now();
    }

    if (status == null) {
        status = EventRegistrationStatus.REGISTERED;
    }
}

public UUID getId() {
    return id;
}

public CampusEvent getEvent() {
    return event;
}

public void setEvent(CampusEvent event) {
    this.event = event;
}

public User getStudent() {
    return student;
}

public void setStudent(User student) {
    this.student = student;
}

public LocalDateTime getRegisteredAt() {
    return registeredAt;
}

public void setRegisteredAt(LocalDateTime registeredAt) {
    this.registeredAt = registeredAt;
}

public EventRegistrationStatus getStatus() {
    return status;
}

public void setStatus(EventRegistrationStatus status) {
    this.status = status;
}
}
