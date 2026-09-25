package com.campusconnect.repository;

import com.campusconnect.model.Exam;
import com.campusconnect.model.ExamResultStatus;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public interface ExamRepository extends JpaRepository<Exam, UUID> {

    List<Exam> findBySectionId(UUID sectionId);

    List<Exam> findBySubjectId(UUID subjectId);

    List<Exam> findBySectionIdAndSubjectId(
            UUID sectionId,
            UUID subjectId
    );

    boolean existsBySectionIdAndExamDateAndStartTimeLessThanAndEndTimeGreaterThan(
            UUID sectionId,
            LocalDate examDate,
            LocalTime endTime,
            LocalTime startTime
    );

    boolean existsBySectionIdAndExamDateAndStartTimeLessThanAndEndTimeGreaterThanAndIdNot(
            UUID sectionId,
            LocalDate examDate,
            LocalTime endTime,
            LocalTime startTime,
            UUID id
    );

    List<Exam> findByExamDate(LocalDate examDate);
    long countByResultStatus(ExamResultStatus resultStatus);
}