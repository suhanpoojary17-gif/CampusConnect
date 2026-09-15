package com.campusconnect.controller;

import com.campusconnect.model.Section;
import com.campusconnect.service.SectionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/sections")
public class SectionController {

    private final SectionService sectionService;

    public SectionController(SectionService sectionService) {
        this.sectionService = sectionService;
    }

    @PostMapping
    public ResponseEntity<Section> createSection(
            @RequestBody Section section) {

        return ResponseEntity.ok(
                sectionService.createSection(section)
        );
    }

    @GetMapping
    public ResponseEntity<List<Section>> getAllSections() {

        return ResponseEntity.ok(
                sectionService.getAllSections()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Section> getSectionById(
            @PathVariable UUID id) {

        return ResponseEntity.ok(
                sectionService.getSectionById(id)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<Section> updateSection(
            @PathVariable UUID id,
            @RequestBody Section section) {

        return ResponseEntity.ok(
                sectionService.updateSection(id, section)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSection(
            @PathVariable UUID id) {

        sectionService.deleteSection(id);

        return ResponseEntity.noContent().build();
    }
}