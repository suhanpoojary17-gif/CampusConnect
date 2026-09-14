package com.campusconnect.repository;

import com.campusconnect.model.Year;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface YearRepository extends JpaRepository<Year, UUID> {
}