package com.campusconnect.controller;

import com.campusconnect.dto.CampusEventResponse;
import com.campusconnect.dto.EventRegistrationResponse;
import com.campusconnect.service.CampusEventService;
import com.campusconnect.service.EventRegistrationService;

import jakarta.validation.constraints.NotNull;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/events")
@PreAuthorize("hasRole('STUDENT')")
public class CampusEventController {

    private final CampusEventService campusEventService;
    private final EventRegistrationService eventRegistrationService;

    public CampusEventController(
            CampusEventService campusEventService,
            EventRegistrationService eventRegistrationService
    ) {
        this.campusEventService = campusEventService;
        this.eventRegistrationService = eventRegistrationService;
    }

    /**
     * Student views all published events.
     */
    @GetMapping
    public ResponseEntity<List<CampusEventResponse>> getPublishedEvents() {

        List<CampusEventResponse> events =
                campusEventService.getPublishedEvents();

        return ResponseEntity.ok(events);
    }

    /**
     * Student views the details of a published event.
     */
    @GetMapping("/{id}")
    public ResponseEntity<CampusEventResponse> getEventDetails(
            @PathVariable UUID id
    ) {
        CampusEventResponse event =
                campusEventService.getPublishedEvent(id);

        return ResponseEntity.ok(event);
    }

    /**
     * Student registers for an event.
     */
    @PostMapping("/{id}/register")
    public ResponseEntity<EventRegistrationResponse> registerForEvent(
            @PathVariable UUID id,
            Authentication authentication
    ) {
        String studentEmail = authentication.getName();

        EventRegistrationResponse registration =
                eventRegistrationService.register(
                        id,
                        studentEmail
                );

        return ResponseEntity.ok(registration);
    }

    /**
     * Student views their registration history.
     */
    @GetMapping("/my-registrations")
    public ResponseEntity<List<EventRegistrationResponse>>
    getMyRegistrations(
            Authentication authentication
    ) {
        String studentEmail = authentication.getName();

        List<EventRegistrationResponse> registrations =
                eventRegistrationService.getMyRegistrations(
                        studentEmail
                );

        return ResponseEntity.ok(registrations);
    }

    /**
     * Student cancels their registration.
     */
    @DeleteMapping("/{id}/register")
    public ResponseEntity<EventRegistrationResponse>
    cancelRegistration(
            @PathVariable UUID id,
            Authentication authentication
    ) {
        String studentEmail = authentication.getName();

        EventRegistrationResponse registration =
                eventRegistrationService.cancelRegistration(
                        id,
                        studentEmail
                );

        return ResponseEntity.ok(registration);
    }
}