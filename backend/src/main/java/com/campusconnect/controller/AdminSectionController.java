package com.campusconnect.controller;

import com.campusconnect.dto.SectionRequest;
import com.campusconnect.model.Department;
import com.campusconnect.model.Section;
import com.campusconnect.model.Semester;
import com.campusconnect.model.Year;
import com.campusconnect.repository.DepartmentRepository;
import com.campusconnect.repository.SectionRepository;
import com.campusconnect.repository.SemesterRepository;
import com.campusconnect.repository.YearRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/sections")
public class AdminSectionController {

    private final SectionRepository sectionRepository;
    private final DepartmentRepository departmentRepository;
    private final YearRepository yearRepository;
    private final SemesterRepository semesterRepository;

    public AdminSectionController(
            SectionRepository sectionRepository,
            DepartmentRepository departmentRepository,
            YearRepository yearRepository,
            SemesterRepository semesterRepository) {

        this.sectionRepository = sectionRepository;
        this.departmentRepository = departmentRepository;
        this.yearRepository = yearRepository;
        this.semesterRepository = semesterRepository;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Section> createSection(
            @RequestBody SectionRequest request) {

        Department department =
                departmentRepository.findById(request.getDepartmentId())
                        .orElseThrow(() ->
                                new RuntimeException("Department not found"));

        Year year =
                yearRepository.findById(request.getYearId())
                        .orElseThrow(() ->
                                new RuntimeException("Academic year not found"));

        Semester semester =
                semesterRepository.findById(request.getSemesterId())
                        .orElseThrow(() ->
                                new RuntimeException("Semester not found"));

        Section section = new Section();

        section.setName(request.getName());
        section.setDepartment(department);
        section.setYear(year);
        section.setSemester(semester);

        return ResponseEntity.ok(
                sectionRepository.save(section)
        );
    }
}