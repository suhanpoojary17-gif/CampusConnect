package com.campusconnect.repository;

import com.campusconnect.entity.Facility;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FacilityRepository extends JpaRepository<Facility, UUID> {

    boolean existsByNameIgnoreCase(String name);
}