package com.campusconnect.repository;

import com.campusconnect.model.Assessment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AssessmentRepository extends JpaRepository<Assessment, UUID> {

    List<Assessment> findBySectionId(UUID sectionId);

    List<Assessment> findBySubjectId(UUID subjectId);

    List<Assessment> findByTeacherId(Long teacherId);

    List<Assessment> findBySectionIdAndSubjectId(
            UUID sectionId,
            UUID subjectId
    );
}