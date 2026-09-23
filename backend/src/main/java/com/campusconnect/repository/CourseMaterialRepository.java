package com.campusconnect.repository;

import com.campusconnect.model.CourseMaterial;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CourseMaterialRepository
        extends JpaRepository<CourseMaterial, UUID> {

    List<CourseMaterial> findBySectionId(UUID sectionId);

    List<CourseMaterial> findBySubjectId(UUID subjectId);

    List<CourseMaterial> findByTeacherId(Long teacherId);

    List<CourseMaterial> findBySectionIdAndSubjectId(
            UUID sectionId,
            UUID subjectId
    );
}