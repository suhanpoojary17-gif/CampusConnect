package com.campusconnect.dto;

public class AttendanceSummaryResponse {

    private long totalClasses;
    private long presentClasses;
    private long absentClasses;
    private double attendancePercentage;

    public AttendanceSummaryResponse(
            long totalClasses,
            long presentClasses,
            long absentClasses,
            double attendancePercentage) {

        this.totalClasses = totalClasses;
        this.presentClasses = presentClasses;
        this.absentClasses = absentClasses;
        this.attendancePercentage = attendancePercentage;
    }

    public long getTotalClasses() {
        return totalClasses;
    }

    public long getPresentClasses() {
        return presentClasses;
    }

    public long getAbsentClasses() {
        return absentClasses;
    }

    public double getAttendancePercentage() {
        return attendancePercentage;
    }
}