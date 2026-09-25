package com.campusconnect.dto;

public class AdminDashboardResponse {

    private long totalUsers;
    private long totalStudents;
    private long totalTeachers;

    private long totalDepartments;
    private long totalAcademicYears;
    private long totalSemesters;
    private long totalSections;
    private long totalSubjects;

    private long totalAssignments;
    private long totalCourseMaterials;
    private long totalAssessments;

    private long totalExams;
    private long totalExamMarks;
    private long totalPublishedResults;

    private long totalNotices;
    private long totalNotifications;

    private long totalFacilities;
    private long totalBookings;

    public AdminDashboardResponse() {
    }

    public AdminDashboardResponse(
            long totalUsers,
            long totalStudents,
            long totalTeachers,
            long totalDepartments,
            long totalAcademicYears,
            long totalSemesters,
            long totalSections,
            long totalSubjects,
            long totalAssignments,
            long totalCourseMaterials,
            long totalAssessments,
            long totalExams,
            long totalExamMarks,
            long totalPublishedResults,
            long totalNotices,
            long totalNotifications,
            long totalFacilities,
            long totalBookings) {

        this.totalUsers = totalUsers;
        this.totalStudents = totalStudents;
        this.totalTeachers = totalTeachers;
        this.totalDepartments = totalDepartments;
        this.totalAcademicYears = totalAcademicYears;
        this.totalSemesters = totalSemesters;
        this.totalSections = totalSections;
        this.totalSubjects = totalSubjects;
        this.totalAssignments = totalAssignments;
        this.totalCourseMaterials = totalCourseMaterials;
        this.totalAssessments = totalAssessments;
        this.totalExams = totalExams;
        this.totalExamMarks = totalExamMarks;
        this.totalPublishedResults = totalPublishedResults;
        this.totalNotices = totalNotices;
        this.totalNotifications = totalNotifications;
        this.totalFacilities = totalFacilities;
        this.totalBookings = totalBookings;
    }

    public long getTotalUsers() {
        return totalUsers;
    }

    public long getTotalStudents() {
        return totalStudents;
    }

    public long getTotalTeachers() {
        return totalTeachers;
    }

    public long getTotalDepartments() {
        return totalDepartments;
    }

    public long getTotalAcademicYears() {
        return totalAcademicYears;
    }

    public long getTotalSemesters() {
        return totalSemesters;
    }

    public long getTotalSections() {
        return totalSections;
    }

    public long getTotalSubjects() {
        return totalSubjects;
    }

    public long getTotalAssignments() {
        return totalAssignments;
    }

    public long getTotalCourseMaterials() {
        return totalCourseMaterials;
    }

    public long getTotalAssessments() {
        return totalAssessments;
    }

    public long getTotalExams() {
        return totalExams;
    }

    public long getTotalExamMarks() {
        return totalExamMarks;
    }

    public long getTotalPublishedResults() {
        return totalPublishedResults;
    }

    public long getTotalNotices() {
        return totalNotices;
    }

    public long getTotalNotifications() {
        return totalNotifications;
    }

    public long getTotalFacilities() {
        return totalFacilities;
    }

    public long getTotalBookings() {
        return totalBookings;
    }
}