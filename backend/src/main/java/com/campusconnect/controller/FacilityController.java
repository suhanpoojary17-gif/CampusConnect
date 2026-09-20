package com.campusconnect.controller;

import com.campusconnect.dto.FacilityRequest;
import com.campusconnect.dto.FacilityResponse;
import com.campusconnect.service.FacilityService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/facilities")
public class FacilityController {

    private final FacilityService facilityService;

    public FacilityController(FacilityService facilityService) {
        this.facilityService = facilityService;
    }

    // Admin: Create facility
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FacilityResponse> createFacility(
            @Valid @RequestBody FacilityRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(facilityService.createFacility(request));
    }

    // Authenticated users: View all facilities
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<FacilityResponse>> getAllFacilities() {

        return ResponseEntity.ok(
                facilityService.getAllFacilities()
        );
    }

    // Authenticated users: View one facility
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<FacilityResponse> getFacilityById(
            @PathVariable UUID id) {

        return ResponseEntity.ok(
                facilityService.getFacilityById(id)
        );
    }

    // Admin: Update facility
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FacilityResponse> updateFacility(
            @PathVariable UUID id,
            @Valid @RequestBody FacilityRequest request) {

        return ResponseEntity.ok(
                facilityService.updateFacility(id, request)
        );
    }

    // Admin: Delete facility
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteFacility(
            @PathVariable UUID id) {

        facilityService.deleteFacility(id);

        return ResponseEntity.noContent().build();
    }
}