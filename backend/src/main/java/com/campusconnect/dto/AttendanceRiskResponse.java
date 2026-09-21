package com.campusconnect.dto;

import com.campusconnect.entity.AttendanceRiskLevel;

public class AttendanceRiskResponse {

    private double attendancePercentage;
    private AttendanceRiskLevel riskLevel;
    private long presentClasses;
    private long totalClasses;
    private long absentClasses;
    private long classesNeededFor75;

    public AttendanceRiskResponse(
            double attendancePercentage,
            AttendanceRiskLevel riskLevel,
            long presentClasses,
            long totalClasses,
            long absentClasses,
            long classesNeededFor75) {

        this.attendancePercentage = attendancePercentage;
        this.riskLevel = riskLevel;
        this.presentClasses = presentClasses;
        this.totalClasses = totalClasses;
        this.absentClasses = absentClasses;
        this.classesNeededFor75 = classesNeededFor75;
    }

    public double getAttendancePercentage() {
        return attendancePercentage;
    }

    public AttendanceRiskLevel getRiskLevel() {
        return riskLevel;
    }

    public long getPresentClasses() {
        return presentClasses;
    }

    public long getTotalClasses() {
        return totalClasses;
    }

    public long getAbsentClasses() {
        return absentClasses;
    }

    public long getClassesNeededFor75() {
        return classesNeededFor75;
    }
}