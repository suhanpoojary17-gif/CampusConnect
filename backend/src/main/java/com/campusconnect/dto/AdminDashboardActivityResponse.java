package com.campusconnect.dto;

import java.util.List;

public class AdminDashboardActivityResponse {

    private List<AssignmentActivityResponse> upcomingAssignments;
    private List<NoticeActivityResponse> recentNotices;
    private List<ExamActivityResponse> upcomingExams;
    private List<PublishedResultActivityResponse> recentPublishedResults;
    private List<BookingActivityResponse> recentBookings;
    private List<NotificationActivityResponse> recentNotifications;

    public AdminDashboardActivityResponse() {
    }

    public AdminDashboardActivityResponse(
            List<AssignmentActivityResponse> upcomingAssignments,
            List<NoticeActivityResponse> recentNotices,
            List<ExamActivityResponse> upcomingExams,
            List<PublishedResultActivityResponse> recentPublishedResults,
            List<BookingActivityResponse> recentBookings,
            List<NotificationActivityResponse> recentNotifications) {

        this.upcomingAssignments = upcomingAssignments;
        this.recentNotices = recentNotices;
        this.upcomingExams = upcomingExams;
        this.recentPublishedResults = recentPublishedResults;
        this.recentBookings = recentBookings;
        this.recentNotifications = recentNotifications;
    }

    public List<AssignmentActivityResponse> getUpcomingAssignments() {
        return upcomingAssignments;
    }

    public void setUpcomingAssignments(
            List<AssignmentActivityResponse> upcomingAssignments) {

        this.upcomingAssignments = upcomingAssignments;
    }

    public List<NoticeActivityResponse> getRecentNotices() {
        return recentNotices;
    }

    public void setRecentNotices(
            List<NoticeActivityResponse> recentNotices) {

        this.recentNotices = recentNotices;
    }

    public List<ExamActivityResponse> getUpcomingExams() {
        return upcomingExams;
    }

    public void setUpcomingExams(List<ExamActivityResponse> upcomingExams) {
        this.upcomingExams = upcomingExams;
    }

    public List<PublishedResultActivityResponse> getRecentPublishedResults() {
        return recentPublishedResults;
    }

    public void setRecentPublishedResults(
            List<PublishedResultActivityResponse> recentPublishedResults) {

        this.recentPublishedResults = recentPublishedResults;
    }

    public List<BookingActivityResponse> getRecentBookings() {
        return recentBookings;
    }

    public void setRecentBookings(List<BookingActivityResponse> recentBookings) {
        this.recentBookings = recentBookings;
    }

    public List<NotificationActivityResponse> getRecentNotifications() {
        return recentNotifications;
    }

    public void setRecentNotifications(
            List<NotificationActivityResponse> recentNotifications) {

        this.recentNotifications = recentNotifications;
    }

    public static class AssignmentActivityResponse {

        private String id;
        private String title;
        private String subject;
        private String section;
        private String dueDateTime;

        public AssignmentActivityResponse() {
        }

        public AssignmentActivityResponse(
                String id,
                String title,
                String subject,
                String section,
                String dueDateTime) {

            this.id = id;
            this.title = title;
            this.subject = subject;
            this.section = section;
            this.dueDateTime = dueDateTime;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getSubject() {
            return subject;
        }

        public void setSubject(String subject) {
            this.subject = subject;
        }

        public String getSection() {
            return section;
        }

        public void setSection(String section) {
            this.section = section;
        }

        public String getDueDateTime() {
            return dueDateTime;
        }

        public void setDueDateTime(String dueDateTime) {
            this.dueDateTime = dueDateTime;
        }
    }

    public static class NoticeActivityResponse {

        private String id;
        private String title;
        private String description;
        private String date;
        private String section;

        public NoticeActivityResponse() {
        }

        public NoticeActivityResponse(
                String id,
                String title,
                String description,
                String date,
                String section) {

            this.id = id;
            this.title = title;
            this.description = description;
            this.date = date;
            this.section = section;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public String getSection() {
            return section;
        }

        public void setSection(String section) {
            this.section = section;
        }
    }

    public static class ExamActivityResponse {

        private String id;
        private String title;
        private String subject;
        private String section;
        private String examDate;
        private String startTime;
        private String endTime;
        private String resultStatus;

        public ExamActivityResponse() {
        }

        public ExamActivityResponse(
                String id,
                String title,
                String subject,
                String section,
                String examDate,
                String startTime,
                String endTime,
                String resultStatus) {

            this.id = id;
            this.title = title;
            this.subject = subject;
            this.section = section;
            this.examDate = examDate;
            this.startTime = startTime;
            this.endTime = endTime;
            this.resultStatus = resultStatus;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getSubject() {
            return subject;
        }

        public void setSubject(String subject) {
            this.subject = subject;
        }

        public String getSection() {
            return section;
        }

        public void setSection(String section) {
            this.section = section;
        }

        public String getExamDate() {
            return examDate;
        }

        public void setExamDate(String examDate) {
            this.examDate = examDate;
        }

        public String getStartTime() {
            return startTime;
        }

        public void setStartTime(String startTime) {
            this.startTime = startTime;
        }

        public String getEndTime() {
            return endTime;
        }

        public void setEndTime(String endTime) {
            this.endTime = endTime;
        }

        public String getResultStatus() {
            return resultStatus;
        }

        public void setResultStatus(String resultStatus) {
            this.resultStatus = resultStatus;
        }
    }

    public static class PublishedResultActivityResponse {

        private String examId;
        private String examTitle;
        private String subject;
        private String section;
        private String examDate;
        private String maximumMarks;
        private String resultStatus;

        public PublishedResultActivityResponse() {
        }

        public PublishedResultActivityResponse(
                String examId,
                String examTitle,
                String subject,
                String section,
                String examDate,
                String maximumMarks,
                String resultStatus) {

            this.examId = examId;
            this.examTitle = examTitle;
            this.subject = subject;
            this.section = section;
            this.examDate = examDate;
            this.maximumMarks = maximumMarks;
            this.resultStatus = resultStatus;
        }

        public String getExamId() {
            return examId;
        }

        public void setExamId(String examId) {
            this.examId = examId;
        }

        public String getExamTitle() {
            return examTitle;
        }

        public void setExamTitle(String examTitle) {
            this.examTitle = examTitle;
        }

        public String getSubject() {
            return subject;
        }

        public void setSubject(String subject) {
            this.subject = subject;
        }

        public String getSection() {
            return section;
        }

        public void setSection(String section) {
            this.section = section;
        }

        public String getExamDate() {
            return examDate;
        }

        public void setExamDate(String examDate) {
            this.examDate = examDate;
        }

        public String getMaximumMarks() {
            return maximumMarks;
        }

        public void setMaximumMarks(String maximumMarks) {
            this.maximumMarks = maximumMarks;
        }

        public String getResultStatus() {
            return resultStatus;
        }

        public void setResultStatus(String resultStatus) {
            this.resultStatus = resultStatus;
        }
    }

    public static class BookingActivityResponse {

        private String id;
        private String facility;
        private String bookedBy;
        private String bookingDate;
        private String startTime;
        private String endTime;
        private String purpose;
        private String status;

        public BookingActivityResponse() {
        }

        public BookingActivityResponse(
                String id,
                String facility,
                String bookedBy,
                String bookingDate,
                String startTime,
                String endTime,
                String purpose,
                String status) {

            this.id = id;
            this.facility = facility;
            this.bookedBy = bookedBy;
            this.bookingDate = bookingDate;
            this.startTime = startTime;
            this.endTime = endTime;
            this.purpose = purpose;
            this.status = status;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getFacility() {
            return facility;
        }

        public void setFacility(String facility) {
            this.facility = facility;
        }

        public String getBookedBy() {
            return bookedBy;
        }

        public void setBookedBy(String bookedBy) {
            this.bookedBy = bookedBy;
        }

        public String getBookingDate() {
            return bookingDate;
        }

        public void setBookingDate(String bookingDate) {
            this.bookingDate = bookingDate;
        }

        public String getStartTime() {
            return startTime;
        }

        public void setStartTime(String startTime) {
            this.startTime = startTime;
        }

        public String getEndTime() {
            return endTime;
        }

        public void setEndTime(String endTime) {
            this.endTime = endTime;
        }

        public String getPurpose() {
            return purpose;
        }

        public void setPurpose(String purpose) {
            this.purpose = purpose;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }

    public static class NotificationActivityResponse {

        private String id;
        private String user;
        private String title;
        private String message;
        private String type;
        private String createdAt;
        private boolean read;

        public NotificationActivityResponse() {
        }

        public NotificationActivityResponse(
                String id,
                String user,
                String title,
                String message,
                String type,
                String createdAt,
                boolean read) {

            this.id = id;
            this.user = user;
            this.title = title;
            this.message = message;
            this.type = type;
            this.createdAt = createdAt;
            this.read = read;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getUser() {
            return user;
        }

        public void setUser(String user) {
            this.user = user;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
        }

        public boolean isRead() {
            return read;
        }

        public void setRead(boolean read) {
            this.read = read;
        }
    }
}