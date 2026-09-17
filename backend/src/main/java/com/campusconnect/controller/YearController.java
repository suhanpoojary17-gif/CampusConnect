package com.campusconnect.controller;

import com.campusconnect.model.Year;
import com.campusconnect.service.YearService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/years")
public class YearController {

    private final YearService yearService;

    public YearController(YearService yearService) {
        this.yearService = yearService;
    }

    @PostMapping
    public ResponseEntity<Year> createYear(
            @Valid @RequestBody Year year) {

        return ResponseEntity.ok(
                yearService.createYear(year)
        );
    }

    @GetMapping
    public ResponseEntity<List<Year>> getAllYears() {

        return ResponseEntity.ok(
                yearService.getAllYears()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Year> getYearById(
            @PathVariable UUID id) {

        return ResponseEntity.ok(
                yearService.getYearById(id)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<Year> updateYear(
            @PathVariable UUID id,
            @Valid @RequestBody Year year) {

        return ResponseEntity.ok(
                yearService.updateYear(id, year)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteYear(
            @PathVariable UUID id) {

        yearService.deleteYear(id);

        return ResponseEntity.noContent().build();
    }
}