package com.campusconnect.repository;

import com.campusconnect.entity.TeacherAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TeacherAssignmentRepository
        extends JpaRepository<TeacherAssignment, Long> {

    Optional<TeacherAssignment> findByTeacherIdAndSectionIdAndSubjectId(
            Long teacherId,
            UUID sectionId,
            UUID subjectId
    );

    boolean existsByTeacherIdAndSectionIdAndSubjectId(
        Long teacherId,
        UUID sectionId,
        UUID subjectId
    );
}