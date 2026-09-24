package com.campusconnect.repository;

import com.campusconnect.model.ExamMark;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ExamMarkRepository
        extends JpaRepository<ExamMark, UUID> {

    Optional<ExamMark> findByExamIdAndStudentId(
            UUID examId,
            UUID studentId
    );

    List<ExamMark> findByExamId(UUID examId);

    List<ExamMark> findByStudentId(UUID studentId);
}