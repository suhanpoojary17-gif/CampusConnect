package com.campusconnect.dto;

import com.campusconnect.entity.AttendanceStatus;

import java.time.LocalDateTime;

public class AttendanceAuditResponse {

    private Long id;
    private Long attendanceId;
    private Long changedBy;
    private AttendanceStatus oldStatus;
    private AttendanceStatus newStatus;
    private LocalDateTime changedAt;

    public AttendanceAuditResponse() {
    }

    public AttendanceAuditResponse(
            Long id,
            Long attendanceId,
            Long changedBy,
            AttendanceStatus oldStatus,
            AttendanceStatus newStatus,
            LocalDateTime changedAt) {

        this.id = id;
        this.attendanceId = attendanceId;
        this.changedBy = changedBy;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.changedAt = changedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAttendanceId() {
        return attendanceId;
    }

    public void setAttendanceId(Long attendanceId) {
        this.attendanceId = attendanceId;
    }

    public Long getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(Long changedBy) {
        this.changedBy = changedBy;
    }

    public AttendanceStatus getOldStatus() {
        return oldStatus;
    }

    public void setOldStatus(AttendanceStatus oldStatus) {
        this.oldStatus = oldStatus;
    }

    public AttendanceStatus getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(AttendanceStatus newStatus) {
        this.newStatus = newStatus;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(LocalDateTime changedAt) {
        this.changedAt = changedAt;
    }
}