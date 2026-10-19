package com.campusconnect.controller;

import com.campusconnect.dto.CampusEventRequest;
import com.campusconnect.dto.CampusEventResponse;
import com.campusconnect.dto.EventRegistrationResponse;
import com.campusconnect.service.CampusEventService;
import com.campusconnect.service.EventRegistrationService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/events")
@PreAuthorize("hasRole('ADMIN')")
public class AdminCampusEventController {

    private final CampusEventService campusEventService;
    private final EventRegistrationService eventRegistrationService;

    public AdminCampusEventController(
            CampusEventService campusEventService,
            EventRegistrationService eventRegistrationService
    ) {
        this.campusEventService = campusEventService;
        this.eventRegistrationService = eventRegistrationService;
    }

    /**
     * Administrator views all events, including unpublished events.
     */
    @GetMapping
    public ResponseEntity<List<CampusEventResponse>> getAllEvents(
            Authentication authentication
    ) {
        String adminEmail = authentication.getName();

        List<CampusEventResponse> events =
                campusEventService.getAllEvents(adminEmail);

        return ResponseEntity.ok(events);
    }

    /**
     * Administrator creates an event.
     * Newly created events remain unpublished.
     */
    @PostMapping
    public ResponseEntity<CampusEventResponse> createEvent(
            @Valid @RequestBody CampusEventRequest request,
            Authentication authentication
    ) {
        String adminEmail = authentication.getName();

        CampusEventResponse event =
                campusEventService.createEvent(
                        request,
                        adminEmail
                );

        return ResponseEntity.ok(event);
    }

    /**
     * Administrator updates an existing event.
     */
    @PutMapping("/{id}")
    public ResponseEntity<CampusEventResponse> updateEvent(
            @PathVariable UUID id,
            @Valid @RequestBody CampusEventRequest request,
            Authentication authentication
    ) {
        String adminEmail = authentication.getName();

        CampusEventResponse event =
                campusEventService.updateEvent(
                        id,
                        request,
                        adminEmail
                );

        return ResponseEntity.ok(event);
    }

    /**
     * Administrator publishes an event.
     */
    @PutMapping("/{id}/publish")
    public ResponseEntity<CampusEventResponse> publishEvent(
            @PathVariable UUID id,
            Authentication authentication
    ) {
        String adminEmail = authentication.getName();

        CampusEventResponse event =
                campusEventService.publishEvent(
                        id,
                        adminEmail
                );

        return ResponseEntity.ok(event);
    }

    /**
     * Administrator unpublishes an event.
     */
    @PutMapping("/{id}/unpublish")
    public ResponseEntity<CampusEventResponse> unpublishEvent(
            @PathVariable UUID id,
            Authentication authentication
    ) {
        String adminEmail = authentication.getName();

        CampusEventResponse event =
                campusEventService.unpublishEvent(
                        id,
                        adminEmail
                );

        return ResponseEntity.ok(event);
    }

    /**
     * Administrator deletes an eligible event.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(
            @PathVariable UUID id,
            Authentication authentication
    ) {
        String adminEmail = authentication.getName();

        campusEventService.deleteEvent(
                id,
                adminEmail
        );

        return ResponseEntity.noContent().build();
    }

    /**
     * Administrator views registrations for an event.
     */
    @GetMapping("/{id}/registrations")
    public ResponseEntity<List<EventRegistrationResponse>>
    getEventRegistrations(
            @PathVariable UUID id,
            Authentication authentication
    ) {
        String adminEmail = authentication.getName();

        List<EventRegistrationResponse> registrations =
                eventRegistrationService.getEventRegistrations(
                        id,
                        adminEmail
                );

        return ResponseEntity.ok(registrations);
    }
}