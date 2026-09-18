package com.campusconnect.repository;

import com.campusconnect.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    boolean existsByStudentIdAndSubjectIdAndAttendanceDate(
            UUID studentId,
            UUID subjectId,
            LocalDate attendanceDate
    );

    List<Attendance> findBySubjectIdAndAttendanceDate(
            UUID subjectId,
            LocalDate attendanceDate
    );

    List<Attendance> findByStudentId(UUID studentId);

    List<Attendance> findByStudentIdAndSubjectId(
            UUID studentId,
            UUID subjectId
    );

    List<Attendance> findBySubjectSectionId(UUID sectionId);
}