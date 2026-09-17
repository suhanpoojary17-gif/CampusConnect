package com.campusconnect.controller;

import com.campusconnect.dto.YearRequest;
import com.campusconnect.model.Year;
import com.campusconnect.repository.YearRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin/academic-years")
public class AdminYearController {

    private final YearRepository yearRepository;

    public AdminYearController(YearRepository yearRepository) {
        this.yearRepository = yearRepository;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Year> createYear(
            @Valid @RequestBody YearRequest request) {

        Year year = new Year();
        year.setName(request.getName());

        return ResponseEntity.ok(
                yearRepository.save(year)
        );
    }
}