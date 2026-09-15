package com.campusconnect.service;

import com.campusconnect.model.Year;
import com.campusconnect.repository.YearRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class YearService {

    private final YearRepository yearRepository;

    public YearService(YearRepository yearRepository) {
        this.yearRepository = yearRepository;
    }

    public Year createYear(Year year) {
        return yearRepository.save(year);
    }

    public List<Year> getAllYears() {
        return yearRepository.findAll();
    }

    public Year getYearById(UUID id) {
        return yearRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Year not found"));
    }

    public Year updateYear(UUID id, Year year) {
        Year existingYear = getYearById(id);

        existingYear.setName(year.getName());

        return yearRepository.save(existingYear);
    }

    public void deleteYear(UUID id) {
        Year year = getYearById(id);
        yearRepository.delete(year);
    }
}