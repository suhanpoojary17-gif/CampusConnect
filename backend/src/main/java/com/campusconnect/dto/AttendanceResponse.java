package com.campusconnect.dto;

import com.campusconnect.entity.AttendanceStatus;

import java.time.LocalDate;
import java.util.UUID;

public class AttendanceResponse {

    private Long id;
    private UUID studentId;
    private UUID subjectId;
    private Long teacherId;
    private LocalDate attendanceDate;
    private AttendanceStatus status;

    public AttendanceResponse() {
    }

    public AttendanceResponse(
            Long id,
            UUID studentId,
            UUID subjectId,
            Long teacherId,
            LocalDate attendanceDate,
            AttendanceStatus status) {

        this.id = id;
        this.studentId = studentId;
        this.subjectId = subjectId;
        this.teacherId = teacherId;
        this.attendanceDate = attendanceDate;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UUID getStudentId() {
        return studentId;
    }

    public void setStudentId(UUID studentId) {
        this.studentId = studentId;
    }

    public UUID getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(UUID subjectId) {
        this.subjectId = subjectId;
    }

    public Long getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(Long teacherId) {
        this.teacherId = teacherId;
    }

    public LocalDate getAttendanceDate() {
        return attendanceDate;
    }

    public void setAttendanceDate(LocalDate attendanceDate) {
        this.attendanceDate = attendanceDate;
    }

    public AttendanceStatus getStatus() {
        return status;
    }

    public void setStatus(AttendanceStatus status) {
        this.status = status;
    }
}