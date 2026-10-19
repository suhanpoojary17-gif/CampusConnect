package com.campusconnect.service;

import com.campusconnect.dto.CampusEventRequest;
import com.campusconnect.dto.CampusEventResponse;
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
public class CampusEventService {

    private final CampusEventRepository eventRepository;
    private final EventRegistrationRepository registrationRepository;
    private final UserRepository userRepository;

    public CampusEventService(
            CampusEventRepository eventRepository,
            EventRegistrationRepository registrationRepository,
            UserRepository userRepository
    ) {
        this.eventRepository = eventRepository;
        this.registrationRepository = registrationRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public CampusEventResponse createEvent(
            CampusEventRequest request,
            String adminEmail
    ) {
        User admin = getUserByEmail(adminEmail);
        requireRole(admin, Role.ADMIN);

        validateEventDates(request);

        CampusEvent event = new CampusEvent();
        applyRequest(event, request);
        event.setPublished(false);
        event.setCreatedBy(admin);

        CampusEvent savedEvent = eventRepository.save(event);

        return mapToResponse(savedEvent);
    }

    @Transactional
    public CampusEventResponse updateEvent(
            UUID eventId,
            CampusEventRequest request,
            String adminEmail
    ) {
        User admin = getUserByEmail(adminEmail);
        requireRole(admin, Role.ADMIN);

        CampusEvent event = eventRepository.findById(eventId)
                .orElseThrow(() ->
                        new RuntimeException("Event not found"));

        validateEventDates(request);

        long activeRegistrations =
                registrationRepository.countByEvent_IdAndStatus(
                        eventId,
                        EventRegistrationStatus.REGISTERED
                );

        Integer requestedCapacity = request.getCapacity();

        if (requestedCapacity != null
                && requestedCapacity < activeRegistrations) {
            throw new RuntimeException(
                    "Capacity cannot be less than the number of active registrations"
            );
        }

        applyRequest(event, request);

        CampusEvent savedEvent = eventRepository.save(event);

        return mapToResponse(savedEvent);
    }

    @Transactional(readOnly = true)
    public List<CampusEventResponse> getPublishedEvents() {
        return eventRepository
                .findByPublishedTrueOrderByStartDateTimeAsc()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CampusEventResponse getPublishedEvent(UUID eventId) {
        CampusEvent event = eventRepository.findById(eventId)
                .orElseThrow(() ->
                        new RuntimeException("Event not found"));

        if (!event.isPublished()) {
            throw new RuntimeException("Event is not available");
        }

        return mapToResponse(event);
    }

    @Transactional(readOnly = true)
    public List<CampusEventResponse> getAllEvents(
            String adminEmail
    ) {
        User admin = getUserByEmail(adminEmail);
        requireRole(admin, Role.ADMIN);

        return eventRepository
                .findAllByOrderByStartDateTimeAsc()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public CampusEventResponse publishEvent(
            UUID eventId,
            String adminEmail
    ) {
        User admin = getUserByEmail(adminEmail);
        requireRole(admin, Role.ADMIN);

        CampusEvent event = eventRepository.findById(eventId)
                .orElseThrow(() ->
                        new RuntimeException("Event not found"));

        event.setPublished(true);

        return mapToResponse(eventRepository.save(event));
    }

    @Transactional
    public CampusEventResponse unpublishEvent(
            UUID eventId,
            String adminEmail
    ) {
        User admin = getUserByEmail(adminEmail);
        requireRole(admin, Role.ADMIN);

        CampusEvent event = eventRepository.findById(eventId)
                .orElseThrow(() ->
                        new RuntimeException("Event not found"));

        event.setPublished(false);

        return mapToResponse(eventRepository.save(event));
    }

    @Transactional
    public void deleteEvent(
            UUID eventId,
            String adminEmail
    ) {
        User admin = getUserByEmail(adminEmail);
        requireRole(admin, Role.ADMIN);

        CampusEvent event = eventRepository.findById(eventId)
                .orElseThrow(() ->
                        new RuntimeException("Event not found"));

        boolean hasRegistrations =
                registrationRepository
                        .existsByEvent_IdAndStatus(
                                eventId,
                                EventRegistrationStatus.REGISTERED
                        );

        if (hasRegistrations) {
            throw new RuntimeException(
                    "An event with active registrations cannot be deleted. Unpublish it instead."
            );
        }

        long totalRegistrations =
                registrationRepository
                        .countByEvent_IdAndStatus(
                                eventId,
                                EventRegistrationStatus.CANCELLED
                        );

        if (totalRegistrations > 0) {
            throw new RuntimeException(
                    "An event with registration history cannot be deleted. Unpublish it instead."
            );
        }

        eventRepository.delete(event);
    }

    private void validateEventDates(
            CampusEventRequest request
    ) {
        LocalDateTime start = request.getStartDateTime();
        LocalDateTime end = request.getEndDateTime();
        LocalDateTime deadline =
                request.getRegistrationDeadline();

        if (start == null || end == null || deadline == null) {
            throw new RuntimeException(
                    "Event start, end and registration deadline are required"
            );
        }

        if (!start.isBefore(end)) {
            throw new RuntimeException(
                    "Event start must be before event end"
            );
        }

        if (deadline.isAfter(start)) {
            throw new RuntimeException(
                    "Registration deadline cannot be after the event starts"
            );
        }

        if (request.getCapacity() != null
                && request.getCapacity() < 1) {
            throw new RuntimeException(
                    "Capacity must be at least 1"
            );
        }
    }

    private void applyRequest(
            CampusEvent event,
            CampusEventRequest request
    ) {
        event.setTitle(request.getTitle().trim());
        event.setDescription(request.getDescription().trim());
        event.setCategory(request.getCategory().trim());
        event.setVenue(request.getVenue().trim());
        event.setStartDateTime(request.getStartDateTime());
        event.setEndDateTime(request.getEndDateTime());
        event.setRegistrationDeadline(
                request.getRegistrationDeadline()
        );
        event.setCapacity(request.getCapacity());
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

    private CampusEventResponse mapToResponse(
            CampusEvent event
    ) {
        long registrationCount =
                registrationRepository.countByEvent_IdAndStatus(
                        event.getId(),
                        EventRegistrationStatus.REGISTERED
                );

        return new CampusEventResponse(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getCategory(),
                event.getVenue(),
                event.getStartDateTime(),
                event.getEndDateTime(),
                event.getRegistrationDeadline(),
                event.getCapacity(),
                event.isPublished(),
                registrationCount,
                event.getCreatedBy().getEmail()
        );
    }
}