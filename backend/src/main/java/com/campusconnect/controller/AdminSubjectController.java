package com.campusconnect.controller;

import com.campusconnect.dto.SubjectRequest;
import com.campusconnect.model.Section;
import com.campusconnect.model.Subject;
import com.campusconnect.repository.SectionRepository;
import com.campusconnect.repository.SubjectRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/subjects")
public class AdminSubjectController {

    private final SubjectRepository subjectRepository;
    private final SectionRepository sectionRepository;

    public AdminSubjectController(
            SubjectRepository subjectRepository,
            SectionRepository sectionRepository) {

        this.subjectRepository = subjectRepository;
        this.sectionRepository = sectionRepository;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Subject> createSubject(
            @RequestBody SubjectRequest request) {

        Section section =
                sectionRepository.findById(request.getSectionId())
                        .orElseThrow(() ->
                                new RuntimeException("Section not found"));

        Subject subject = new Subject();

        subject.setName(request.getName());
        subject.setCode(request.getCode());
        subject.setSection(section);

        return ResponseEntity.ok(
                subjectRepository.save(subject)
        );
    }
}