package com.campusconnect.repository;

import com.campusconnect.model.AssignmentSubmission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AssignmentSubmissionRepository
        extends JpaRepository<AssignmentSubmission, UUID> {

    Optional<AssignmentSubmission> findByAssignmentIdAndStudentId(
            UUID assignmentId,
            UUID studentId
    );

    List<AssignmentSubmission> findByAssignmentId(
            UUID assignmentId
    );

    List<AssignmentSubmission> findByStudentId(
            UUID studentId
    );
}