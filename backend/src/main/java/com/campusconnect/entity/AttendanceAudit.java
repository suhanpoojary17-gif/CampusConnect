package com.campusconnect.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "attendance_audit")
public class AttendanceAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "attendance_id", nullable = false)
    private Attendance attendance;

    @ManyToOne
    @JoinColumn(name = "changed_by", nullable = false)
    private User changedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "old_status", nullable = false)
    private AttendanceStatus oldStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false)
    private AttendanceStatus newStatus;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    public AttendanceAudit() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Attendance getAttendance() {
        return attendance;
    }

    public void setAttendance(Attendance attendance) {
        this.attendance = attendance;
    }

    public User getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(User changedBy) {
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