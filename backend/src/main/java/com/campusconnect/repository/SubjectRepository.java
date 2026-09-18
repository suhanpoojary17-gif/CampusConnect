package com.campusconnect.repository;

import com.campusconnect.model.Subject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SubjectRepository extends JpaRepository<Subject, UUID> {

    boolean existsByIdAndSectionId(UUID subjectId, UUID sectionId);

}