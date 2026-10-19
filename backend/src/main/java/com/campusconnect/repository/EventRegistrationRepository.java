package com.campusconnect.repository;

import com.campusconnect.entity.EventRegistration;
import com.campusconnect.entity.EventRegistrationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EventRegistrationRepository
extends JpaRepository<EventRegistration, UUID> {

Optional<EventRegistration> findByEvent_IdAndStudent_Id(
        UUID eventId,
        Long studentId
);

List<EventRegistration> findByStudent_IdOrderByRegisteredAtDesc(
        Long studentId
);

List<EventRegistration> findByEvent_IdOrderByRegisteredAtAsc(
        UUID eventId
);

long countByEvent_IdAndStatus(
        UUID eventId,
        EventRegistrationStatus status
);

boolean existsByEvent_IdAndStudent_IdAndStatus(
        UUID eventId,
        Long studentId,
        EventRegistrationStatus status
);

boolean existsByEvent_IdAndStatus(
        UUID eventId,
        EventRegistrationStatus status
);

}
