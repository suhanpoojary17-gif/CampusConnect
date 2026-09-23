package com.campusconnect.repository;

import com.campusconnect.model.StudentMark;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StudentMarkRepository extends JpaRepository<StudentMark, UUID> {

    Optional<StudentMark> findByAssessmentIdAndStudentId(
            UUID assessmentId,
            UUID studentId
    );

    List<StudentMark> findByAssessmentId(UUID assessmentId);

    List<StudentMark> findByStudentId(UUID studentId);
}