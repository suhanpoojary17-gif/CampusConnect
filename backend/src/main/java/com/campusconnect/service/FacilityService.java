package com.campusconnect.service;

import com.campusconnect.dto.FacilityRequest;
import com.campusconnect.dto.FacilityResponse;
import com.campusconnect.entity.Facility;
import com.campusconnect.repository.FacilityRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class FacilityService {

    private final FacilityRepository facilityRepository;

    public FacilityService(FacilityRepository facilityRepository) {
        this.facilityRepository = facilityRepository;
    }

    // Create facility
    public FacilityResponse createFacility(FacilityRequest request) {

        if (facilityRepository.existsByNameIgnoreCase(request.getName())) {
            throw new RuntimeException("Facility with this name already exists");
        }

        Facility facility = new Facility(
                request.getName(),
                request.getType(),
                request.getCapacity(),
                request.getLocation()
        );

        Facility savedFacility = facilityRepository.save(facility);

        return mapToResponse(savedFacility);
    }

    // Get all facilities
    public List<FacilityResponse> getAllFacilities() {

        return facilityRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // Get facility by ID
    public FacilityResponse getFacilityById(UUID id) {

        Facility facility = facilityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Facility not found"));

        return mapToResponse(facility);
    }

    // Update facility
    public FacilityResponse updateFacility(UUID id, FacilityRequest request) {

        Facility facility = facilityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Facility not found"));

        if (!facility.getName().equalsIgnoreCase(request.getName())
                && facilityRepository.existsByNameIgnoreCase(request.getName())) {
            throw new RuntimeException("Facility with this name already exists");
        }

        facility.setName(request.getName());
        facility.setType(request.getType());
        facility.setCapacity(request.getCapacity());
        facility.setLocation(request.getLocation());

        Facility updatedFacility = facilityRepository.save(facility);

        return mapToResponse(updatedFacility);
    }

    // Delete facility
    public void deleteFacility(UUID id) {

        if (!facilityRepository.existsById(id)) {
            throw new RuntimeException("Facility not found");
        }

        facilityRepository.deleteById(id);
    }

    // Convert Entity to Response DTO
    private FacilityResponse mapToResponse(Facility facility) {

        return new FacilityResponse(
                facility.getId(),
                facility.getName(),
                facility.getType(),
                facility.getCapacity(),
                facility.getLocation()
        );
    }
}