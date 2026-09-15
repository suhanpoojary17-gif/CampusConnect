package com.campusconnect.service;

import com.campusconnect.model.Section;
import com.campusconnect.repository.SectionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class SectionService {

    private final SectionRepository sectionRepository;

    public SectionService(SectionRepository sectionRepository) {
        this.sectionRepository = sectionRepository;
    }

    public Section createSection(Section section) {
        return sectionRepository.save(section);
    }

    public List<Section> getAllSections() {
        return sectionRepository.findAll();
    }

    public Section getSectionById(UUID id) {
        return sectionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Section not found"));
    }

    public Section updateSection(UUID id, Section section) {

        Section existingSection = getSectionById(id);

        existingSection.setName(section.getName());
        existingSection.setDepartment(section.getDepartment());
        existingSection.setYear(section.getYear());
        existingSection.setSemester(section.getSemester());

        return sectionRepository.save(existingSection);
    }

    public void deleteSection(UUID id) {

        Section section = getSectionById(id);

        sectionRepository.delete(section);
    }
}