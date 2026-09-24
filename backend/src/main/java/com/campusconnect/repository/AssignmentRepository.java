package com.campusconnect.repository;

import com.campusconnect.model.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface AssignmentRepository
        extends JpaRepository<Assignment, UUID> {

    List<Assignment> findBySectionId(UUID sectionId);

    List<Assignment> findBySubjectId(UUID subjectId);

    List<Assignment> findByTeacherId(Long teacherId);

    List<Assignment> findBySectionIdAndSubjectId(
            UUID sectionId,
            UUID subjectId
    );

    List<Assignment> findByDueDateTimeBetween(
            LocalDateTime start,
            LocalDateTime end
    );
}