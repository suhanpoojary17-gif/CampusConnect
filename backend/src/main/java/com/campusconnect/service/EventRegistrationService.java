package com.campusconnect.service;

import com.campusconnect.dto.EventRegistrationResponse;
import com.campusconnect.entity.CampusEvent;
import com.campusconnect.entity.EventRegistration;
import com.campusconnect.entity.EventRegistrationStatus;
import com.campusconnect.entity.Role;
import com.campusconnect.entity.User;
import com.campusconnect.repository.CampusEventRepository;
import com.campusconnect.repository.EventRegistrationRepository;
import com.campusconnect.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class EventRegistrationService {

    private final CampusEventRepository eventRepository;
    private final EventRegistrationRepository registrationRepository;
    private final UserRepository userRepository;

    public EventRegistrationService(
            CampusEventRepository eventRepository,
            EventRegistrationRepository registrationRepository,
            UserRepository userRepository
    ) {
        this.eventRepository = eventRepository;
        this.registrationRepository = registrationRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public EventRegistrationResponse register(
            UUID eventId,
            String studentEmail
    ) {
        CampusEvent event = eventRepository
                .findByIdForUpdate(eventId)
                .orElseThrow(() ->
                        new RuntimeException("Event not found"));

        User student = getUserByEmail(studentEmail);
        requireRole(student, Role.STUDENT);

        if (!event.isPublished()) {
            throw new RuntimeException(
                    "This event is not open for registration"
            );
        }

        LocalDateTime now = LocalDateTime.now();

        if (!event.getRegistrationDeadline().isAfter(now)) {
            throw new RuntimeException(
                    "The registration deadline has passed"
            );
        }

        if (!event.getEndDateTime().isAfter(now)) {
            throw new RuntimeException(
                    "Registration is not available for an event that has ended"
            );
        }

        EventRegistration existing = registrationRepository
                .findByEvent_IdAndStudent_Id(
                        eventId,
                        student.getId()
                )
                .orElse(null);

        if (existing != null
                && existing.getStatus()
                == EventRegistrationStatus.REGISTERED) {
            throw new RuntimeException(
                    "You are already registered for this event"
            );
        }

        long activeRegistrations =
                registrationRepository.countByEvent_IdAndStatus(
                        eventId,
                        EventRegistrationStatus.REGISTERED
                );

        if (event.getCapacity() != null
                && activeRegistrations >= event.getCapacity()) {
            throw new RuntimeException(
                    "This event has reached its registration capacity"
            );
        }

        EventRegistration registration;

        if (existing != null) {
            registration = existing;
            registration.setStatus(
                    EventRegistrationStatus.REGISTERED
            );
            registration.setRegisteredAt(now);
        } else {
            registration = new EventRegistration();
            registration.setEvent(event);
            registration.setStudent(student);
            registration.setRegisteredAt(now);
            registration.setStatus(
                    EventRegistrationStatus.REGISTERED
            );
        }

        EventRegistration saved =
                registrationRepository.save(registration);

        return mapToResponse(saved);
    }

    @Transactional
    public EventRegistrationResponse cancelRegistration(
            UUID eventId,
            String studentEmail
    ) {
        CampusEvent event = eventRepository
                .findByIdForUpdate(eventId)
                .orElseThrow(() ->
                        new RuntimeException("Event not found"));

        User student = getUserByEmail(studentEmail);
        requireRole(student, Role.STUDENT);

        EventRegistration registration =
                registrationRepository
                        .findByEvent_IdAndStudent_Id(
                                eventId,
                                student.getId()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Registration not found"
                                ));

        if (registration.getStatus()
                == EventRegistrationStatus.CANCELLED) {
            throw new RuntimeException(
                    "This registration has already been cancelled"
            );
        }

        if (!event.getStartDateTime()
                .isAfter(LocalDateTime.now())) {
            throw new RuntimeException(
                    "Registration cannot be cancelled after the event has started"
            );
        }

        registration.setStatus(
                EventRegistrationStatus.CANCELLED
        );

        EventRegistration saved =
                registrationRepository.save(registration);

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<EventRegistrationResponse> getMyRegistrations(
            String studentEmail
    ) {
        User student = getUserByEmail(studentEmail);
        requireRole(student, Role.STUDENT);

        return registrationRepository
                .findByStudent_IdOrderByRegisteredAtDesc(
                        student.getId()
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EventRegistrationResponse> getEventRegistrations(
            UUID eventId,
            String adminEmail
    ) {
        User admin = getUserByEmail(adminEmail);
        requireRole(admin, Role.ADMIN);

        if (!eventRepository.existsById(eventId)) {
            throw new RuntimeException("Event not found");
        }

        return registrationRepository
                .findByEvent_IdOrderByRegisteredAtAsc(eventId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));
    }

    private void requireRole(User user, Role expectedRole) {
        if (user.getRole() != expectedRole) {
            throw new RuntimeException(
                    "You are not authorized to perform this action"
            );
        }
    }

    private EventRegistrationResponse mapToResponse(
            EventRegistration registration
    ) {
        CampusEvent event = registration.getEvent();
        User student = registration.getStudent();

        return new EventRegistrationResponse(
                registration.getId(),
                event.getId(),
                event.getTitle(),
                event.getVenue(),
                event.getStartDateTime(),
                event.getEndDateTime(),
                registration.getRegisteredAt(),
                registration.getStatus(),
                student.getId(),
                student.getEmail()
        );
    }
}