package com.campusconnect.repository;

import com.campusconnect.entity.AttendanceAudit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AttendanceAuditRepository extends JpaRepository<AttendanceAudit, Long> {

    List<AttendanceAudit> findByAttendanceIdOrderByChangedAtDesc(Long attendanceId);

    List<AttendanceAudit> findByChangedByIdOrderByChangedAtDesc(Long teacherId);
}