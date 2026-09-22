package com.campusconnect.repository;

import com.campusconnect.model.Subject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubjectRepository extends JpaRepository<Subject, UUID> {

    boolean existsByIdAndSectionId(UUID subjectId, UUID sectionId);

    Optional<Subject> findByCodeIgnoreCase(String code);

    Optional<Subject> findByNameIgnoreCase(String name);

    List<Subject> findBySectionId(UUID sectionId);
}