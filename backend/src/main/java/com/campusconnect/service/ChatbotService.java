package com.campusconnect.service;

import com.campusconnect.dto.AcademicPerformanceResponse;
import com.campusconnect.dto.AssessmentResponse;
import com.campusconnect.dto.AssignmentResponse;
import com.campusconnect.dto.AttendanceSummaryResponse;
import com.campusconnect.dto.BookingResponse;
import com.campusconnect.dto.BusPassResponse;
import com.campusconnect.dto.CourseMaterialResponse;
import com.campusconnect.dto.ExamResponse;
import com.campusconnect.dto.FacilityResponse;
import com.campusconnect.dto.NoticeResponse;
import com.campusconnect.dto.StudentExamResultResponse;
import com.campusconnect.dto.StudentMarkResponse;
import com.campusconnect.dto.SubjectPerformanceResponse;
import com.campusconnect.dto.TimetableResponse;
import com.campusconnect.entity.Notification;
import com.campusconnect.entity.User;
import com.campusconnect.model.Student;
import com.campusconnect.model.Subject;
import com.campusconnect.repository.AssessmentRepository;
import com.campusconnect.repository.ExamMarkRepository;
import com.campusconnect.repository.StudentRepository;
import com.campusconnect.repository.SubjectRepository;
import com.campusconnect.repository.UserRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
public class ChatbotService {

    private final AttendanceService attendanceService;
    private final SubjectRepository subjectRepository;
    private final StudentRepository studentRepository;
    private final TimetableService timetableService;
    private final UserRepository userRepository;
    private final NoticeService noticeService;
    private final NotificationService notificationService;
    private final BusPassService busPassService;
    private final FacilityService facilityService;
    private final BookingService bookingService;

    private final AssignmentService assignmentService;
    private final CourseMaterialService courseMaterialService;
    private final AssessmentRepository assessmentRepository;
    private final StudentMarkService studentMarkService;
    private final ExamMarkRepository examMarkRepository;
    private final StudentExamService studentExamService;
    private final StudentExamResultService studentExamResultService;
    private final AcademicPerformanceService academicPerformanceService;

    public ChatbotService(
            AttendanceService attendanceService,
            SubjectRepository subjectRepository,
            StudentRepository studentRepository,
            TimetableService timetableService,
            UserRepository userRepository,
            NoticeService noticeService,
            NotificationService notificationService,
            BusPassService busPassService,
            FacilityService facilityService,
            BookingService bookingService,
            AssignmentService assignmentService,
            CourseMaterialService courseMaterialService,
            AssessmentRepository assessmentRepository,
            StudentMarkService studentMarkService,
            ExamMarkRepository examMarkRepository,
            StudentExamService studentExamService,
            StudentExamResultService studentExamResultService,
            AcademicPerformanceService academicPerformanceService) {

        this.attendanceService = attendanceService;
        this.subjectRepository = subjectRepository;
        this.studentRepository = studentRepository;
        this.timetableService = timetableService;
        this.userRepository = userRepository;
        this.noticeService = noticeService;
        this.notificationService = notificationService;
        this.busPassService = busPassService;
        this.facilityService = facilityService;
        this.bookingService = bookingService;

        this.assignmentService = assignmentService;
        this.courseMaterialService = courseMaterialService;
        this.assessmentRepository = assessmentRepository;
        this.studentMarkService = studentMarkService;
        this.examMarkRepository = examMarkRepository;
        this.studentExamService = studentExamService;
        this.studentExamResultService = studentExamResultService;
        this.academicPerformanceService = academicPerformanceService;
    }

    public String processMessage(String message, String userEmail) {

        if (message == null || message.trim().isEmpty()) {
            return "Please enter a message.";
        }

        String input = message.trim()
                .toLowerCase()
                .replaceAll("[?!.,]+$", "")
                .trim();

        // =========================================================
        // GREETING
        // =========================================================

        if (input.equals("hello")
                || input.equals("hi")
                || input.equals("hey")
                || input.equals("good morning")
                || input.equals("good afternoon")
                || input.equals("good evening")
                || input.startsWith("hello ")
                || input.startsWith("hi ")
                || input.startsWith("hey ")) {

            return "Hello! I'm CampusConnect Assistant. "
                    + "How can I help you today? "
                    + "Type 'help' to see what I can do.";
        }

        // =========================================================
        // THANKS
        // =========================================================

        if (input.equals("thanks")
                || input.equals("thank you")
                || input.equals("thankyou")
                || input.equals("thanks a lot")
                || input.equals("thank you so much")) {

            return "You're welcome! I'm happy to help.";
        }

        // =========================================================
        // GOODBYE
        // =========================================================

        if (input.equals("bye")
                || input.equals("goodbye")
                || input.equals("see you")
                || input.equals("see you later")) {

            return "Goodbye! Have a great day.";
        }

        // =========================================================
        // HELP
        // =========================================================

        if (input.equals("help")
                || input.equals("what can you do")
                || input.equals("what can you help me with")
                || input.equals("what can you help")
                || input.contains("what can you do for me")
                || input.contains("how can you help me")) {

            return "I can help you with:\n"
                    + "• Attendance and attendance percentage\n"
                    + "• Attendance risk and classes needed for 75%\n"
                    + "• Attendance forecast\n"
                    + "• Today's, tomorrow's and weekly timetable\n"
                    + "• Subject information\n"
                    + "• Assignments and assignment deadlines\n"
                    + "• Course materials and study notes\n"
                    + "• Assessments and internal marks\n"
                    + "• Exam timetable and exam marks\n"
                    + "• Published examination results\n"
                    + "• Academic performance\n"
                    + "• Notices and announcements\n"
                    + "• Notifications and unread notifications\n"
                    + "• Bus pass details, status, route and expiry\n"
                    + "• Available facilities\n"
                    + "• Your facility bookings\n\n"
                    + "Examples:\n"
                    + "• What is my attendance in DBMS?\n"
                    + "• Show my timetable today\n"
                    + "• What assignments do I have?\n"
                    + "• What assignments are due soon?\n"
                    + "• Show my DBMS notes\n"
                    + "• What are my internal marks?\n"
                    + "• What exams do I have?\n"
                    + "• When is my DBMS exam?\n"
                    + "• Show my results\n"
                    + "• How am I performing?\n"
                    + "• Show my latest notices\n"
                    + "• How many unread notifications do I have?\n"
                    + "• Is my bus pass active?";
        }

        // =========================================================
        // RESULTS
        // Put before generic timetable because questions such as
        // "when is my exam" must not be confused with timetable.
        // =========================================================

        if (input.contains("result")
                || input.contains("results")
                || input.contains("result card")
                || input.contains("exam result")) {

            Optional<Subject> subjectOptional =
                    findSubjectFromMessage(input, userEmail);

            if (subjectOptional.isPresent()) {
                return getSubjectResultsResponse(
                        subjectOptional.get().getId(),
                        userEmail
                );
            }

            return getMyResultsResponse(userEmail);
        }

        // =========================================================
        // ACADEMIC PERFORMANCE
        // =========================================================

        if (input.contains("performance")
                || input.contains("academic performance")
                || input.contains("how am i performing")
                || input.contains("how am i doing")
                || input.contains("overall performance")
                || input.contains("overall percentage")) {

            return getMyPerformanceResponse(userEmail);
        }

        // =========================================================
        // EXAMS
        // =========================================================

        if (input.contains("exam")
                || input.contains("exams")
                || input.contains("examination")
                || input.contains("semester exam")) {

            // Exam marks
            if (input.contains("marks")
                    || input.contains("mark")
                    || input.contains("score")
                    || input.contains("obtained")) {

                Optional<Subject> subjectOptional =
                        findSubjectFromMessage(input, userEmail);

                if (subjectOptional.isPresent()) {
                    return getSubjectExamMarksResponse(
                            subjectOptional.get().getId(),
                            userEmail
                    );
                }

                return getMyExamMarksResponse(userEmail);
            }

            // Specific exam
            Optional<Subject> subjectOptional =
                    findSubjectFromMessage(input, userEmail);

            if (subjectOptional.isPresent()) {
                return getSubjectExamTimetableResponse(
                        subjectOptional.get().getId(),
                        userEmail
                );
            }

            return getMyExamTimetableResponse(userEmail);
        }

        // =========================================================
        // ASSIGNMENTS
        // =========================================================

        if (input.contains("assignment")
                || input.contains("assignments")
                || input.contains("homework")
                || input.contains("home work")) {

            // Due soon / upcoming assignments
            if (input.contains("due")
                    || input.contains("deadline")
                    || input.contains("upcoming")
                    || input.contains("soon")
                    || input.contains("next assignment")) {

                Optional<Subject> subjectOptional =
                        findSubjectFromMessage(input, userEmail);

                if (subjectOptional.isPresent()) {
                    return getUpcomingAssignmentsResponse(
                            userEmail,
                            subjectOptional.get().getId()
                    );
                }

                return getUpcomingAssignmentsResponse(
                        userEmail,
                        null
                );
            }

            // Subject-specific assignments
            Optional<Subject> subjectOptional =
                    findSubjectFromMessage(input, userEmail);

            if (subjectOptional.isPresent()) {
                return getSubjectAssignmentsResponse(
                        userEmail,
                        subjectOptional.get().getId()
                );
            }

            return getMyAssignmentsResponse(userEmail);
        }

        // =========================================================
        // COURSE MATERIALS / NOTES
        // =========================================================

        if (input.contains("course material")
                || input.contains("course materials")
                || input.contains("material")
                || input.contains("materials")
                || input.contains("notes")
                || input.contains("study material")
                || input.contains("study materials")
                || input.contains("textbook")
                || input.contains("textbooks")
                || input.contains("study notes")) {

            Optional<Subject> subjectOptional =
                    findSubjectFromMessage(input, userEmail);

            if (subjectOptional.isPresent()) {
                return getSubjectMaterialsResponse(
                        userEmail,
                        subjectOptional.get().getId()
                );
            }

            return getMyCourseMaterialsResponse(userEmail);
        }

        // =========================================================
        // ASSESSMENTS / INTERNALS
        // =========================================================

        if (input.contains("assessment")
                || input.contains("assessments")
                || input.contains("internal")
                || input.contains("internals")
                || input.contains("internal assessment")
                || input.contains("internal assessments")) {

            // Marks query
            if (input.contains("mark")
                    || input.contains("marks")
                    || input.contains("score")
                    || input.contains("scores")
                    || input.contains("percentage")
                    || input.contains("grade")) {

                Optional<Subject> subjectOptional =
                        findSubjectFromMessage(input, userEmail);

                if (subjectOptional.isPresent()) {
                    return getSubjectInternalMarksResponse(
                            userEmail,
                            subjectOptional.get().getId()
                    );
                }

                return getMyInternalMarksResponse(userEmail);
            }

            Optional<Subject> subjectOptional =
                    findSubjectFromMessage(input, userEmail);

            if (subjectOptional.isPresent()) {
                return getSubjectAssessmentsResponse(
                        userEmail,
                        subjectOptional.get().getId()
                );
            }

            return getMyAssessmentsResponse(userEmail);
        }

        // =========================================================
        // GENERIC MARKS
        // =========================================================

        if (input.contains("marks")
                || input.contains("my marks")
                || input.contains("my mark")
                || input.contains("scores")
                || input.contains("score")) {

            Optional<Subject> subjectOptional =
                    findSubjectFromMessage(input, userEmail);

            if (subjectOptional.isPresent()) {
                return getSubjectInternalMarksResponse(
                        userEmail,
                        subjectOptional.get().getId()
                );
            }

            return getMyInternalMarksResponse(userEmail);
        }

        // =========================================================
        // TIMETABLE
        // =========================================================

        if (input.contains("timetable")
                || input.contains("class")
                || input.contains("classes")
                || input.contains("when is")
                || input.contains("when do")
                || input.contains("what time")) {

            // Weekly timetable
            if (input.contains("weekly")
                    || input.contains("week")
                    || input.contains("this week")) {

                return getWeeklyTimetableResponse(userEmail);
            }

            // Tomorrow's timetable
            if (input.contains("tomorrow")) {

                return getTomorrowTimetableResponse(userEmail);
            }

            // Today's timetable
            if (input.contains("today")
                    || input.contains("now")) {

                return getTodayTimetableResponse(userEmail);
            }

            // Subject-specific timetable
            Optional<Subject> subjectOptional =
                    findSubjectFromMessage(input, userEmail);

            if (subjectOptional.isPresent()) {

                Subject subject = subjectOptional.get();

                return getSubjectTimetableResponse(
                        subject.getId(),
                        userEmail
                );
            }

            return "Please specify today, tomorrow, weekly, or a subject. "
                    + "For example: What is my timetable today?";
        }

        // =========================================================
        // NOTIFICATIONS
        // =========================================================

        if (input.contains("notification")
                || input.contains("notifications")
                || input.contains("alert")
                || input.contains("alerts")) {

            if (input.contains("unread")
                    || input.contains("new")
                    || input.contains("not read")) {

                return getUnreadNotificationsResponse(userEmail);
            }

            if (input.contains("how many")
                    || input.contains("count")) {

                return getUnreadNotificationCountResponse(userEmail);
            }

            if (input.contains("today")) {

                return getTodayNotificationsResponse(userEmail);
            }

            if (input.contains("latest")
                    || input.contains("recent")) {

                return getLatestNotificationsResponse(userEmail);
            }

            return getAllNotificationsResponse(userEmail);
        }

        // =========================================================
        // NOTICES
        // =========================================================

        if (input.contains("notice")
                || input.contains("notices")
                || input.contains("announcement")
                || input.contains("announcements")) {

            if (input.contains("today")) {

                return getTodayNoticesResponse(userEmail);
            }

            if (input.contains("latest")
                    || input.contains("new")
                    || input.contains("recent")) {

                return getLatestNoticesResponse(userEmail);
            }

            return getAllNoticesResponse(userEmail);
        }

        // =========================================================
        // BUS PASS
        // =========================================================

        if (input.contains("bus pass")
                || input.contains("buspass")
                || input.contains("bus-pass")) {

            if (input.contains("status")
                    || input.contains("active")
                    || input.contains("approved")
                    || input.contains("valid")
                    || input.contains("pending")
                    || input.contains("expired")) {

                return getBusPassStatusResponse(userEmail);
            }

            if (input.contains("expire")
                    || input.contains("expiry")
                    || input.contains("expires")) {

                return getBusPassExpiryResponse(userEmail);
            }

            if (input.contains("route")) {

                return getBusPassRouteResponse(userEmail);
            }

            return getBusPassDetailsResponse(userEmail);
        }

        // =========================================================
        // FACILITIES AND BOOKINGS
        // =========================================================

        if (input.contains("facility")
                || input.contains("facilities")
                || input.contains("booking")
                || input.contains("bookings")
                || input.contains("lab")
                || input.contains("room")) {

            if (input.contains("my booking")
                    || input.contains("my bookings")
                    || input.contains("my reservation")
                    || input.contains("my reservations")) {

                return getMyBookingsResponse(userEmail);
            }

            if (input.contains("availability")
                    || input.contains("available")
                    || input.contains("free")) {

                return getFacilitiesResponse();
            }

            return getFacilitiesResponse();
        }

        // =========================================================
        // SUBJECTS
        // =========================================================

        if (input.contains("subject")
                || input.contains("subjects")) {

            Optional<Subject> subjectOptional =
                    findSubjectFromMessage(input, userEmail);

            if (subjectOptional.isPresent()) {

                Subject subject = subjectOptional.get();

                return getSubjectDetailsResponse(
                        subject,
                        userEmail
                );
            }

            return getMySubjectsResponse(userEmail);
        }

        // =========================================================
        // ATTENDANCE
        // =========================================================

        if (input.contains("attendance")
                || input.contains("risk")
                || input.contains("at risk")
                || input.contains("safe")
                || input.contains("how many classes")
                || input.contains("forecast")
                || input.contains("future")
                || input.contains("next classes")) {

            // Forecast
            if (input.contains("forecast")
                    || input.contains("future")
                    || input.contains("next classes")) {

                Optional<Subject> subjectOptional =
                        findSubjectFromMessage(input, userEmail);

                if (subjectOptional.isPresent()) {

                    Subject subject = subjectOptional.get();

                    return getSubjectAttendanceForecastResponse(
                            subject.getId(),
                            userEmail
                    );
                }

                return "Please mention the subject name or subject code. "
                        + "For example: Show my DBMS attendance forecast.";
            }

            // Classes needed to reach 75%
            if (input.contains("how many classes")
                    && (input.contains("75")
                    || input.contains("reach 75")
                    || input.contains("get 75"))) {

                Optional<Subject> subjectOptional =
                        findSubjectFromMessage(input, userEmail);

                if (subjectOptional.isPresent()) {

                    Subject subject = subjectOptional.get();

                    return getClassesNeededFor75Response(
                            subject.getId(),
                            userEmail
                    );
                }

                return "Please mention the subject name or subject code. "
                        + "For example: How many classes do I need to reach 75% in DBMS?";
            }

            // Attendance risk
            if (input.contains("risk")
                    || input.contains("at risk")
                    || input.contains("safe")) {

                Optional<Subject> subjectOptional =
                        findSubjectFromMessage(input, userEmail);

                if (subjectOptional.isPresent()) {

                    Subject subject = subjectOptional.get();

                    return getSubjectAttendanceRiskResponse(
                            subject.getId(),
                            userEmail
                    );
                }

                return "Please mention the subject name or subject code. "
                        + "For example: What is my DBMS attendance risk?";
            }

            // All-subject attendance
            if (input.contains("all")
                    || input.contains("every subject")
                    || input.contains("all subjects")
                    || input.contains("subject wise")
                    || input.contains("subject-wise")) {

                return getAllSubjectAttendanceResponse(userEmail);
            }

            // Subject-specific attendance
            Optional<Subject> subjectOptional =
                    findSubjectFromMessage(input, userEmail);

            if (subjectOptional.isPresent()) {

                Subject subject = subjectOptional.get();

                return getSubjectAttendanceResponse(
                        subject.getId(),
                        userEmail
                );
            }

            return "Please mention the subject name or subject code. "
                    + "For example: What is my attendance in DBMS?";
        }

        // =========================================================
        // UNKNOWN
        // =========================================================

        return "I'm not sure how to answer that yet.\n"
                + "I can help with attendance, timetable, subjects, "
                + "assignments, course materials, assessments, marks, "
                + "exams, results, academic performance, notices, "
                + "notifications, bus passes, facilities and bookings.\n"
                + "Type 'help' to see some example questions.";
    }

    // =============================================================
    // SUBJECT FINDER
    // =============================================================

    private Optional<Subject> findSubjectFromMessage(
            String input,
            String userEmail) {

        Student student =
                studentRepository.findByUserEmail(userEmail);

        if (student == null
                || student.getSection() == null) {

            return Optional.empty();
        }

        UUID sectionId =
                student.getSection().getId();

        List<Subject> subjects =
                subjectRepository.findBySectionId(sectionId);

        if (subjects == null || subjects.isEmpty()) {
            return Optional.empty();
        }

        // Search by subject code first
        for (Subject subject : subjects) {

            if (subject.getCode() != null
                    && input.contains(
                            subject.getCode().toLowerCase())) {

                return Optional.of(subject);
            }
        }

        // Search by subject name
        for (Subject subject : subjects) {

            if (subject.getName() != null
                    && input.contains(
                            subject.getName().toLowerCase())) {

                return Optional.of(subject);
            }
        }

        return Optional.empty();
    }

    // =============================================================
    // ASSIGNMENTS
    // =============================================================

    private String getMyAssignmentsResponse(
            String userEmail) {

        List<AssignmentResponse> assignments =
                assignmentService.getAssignments(userEmail);

        if (assignments == null || assignments.isEmpty()) {
            return "You do not have any assignments.";
        }

        assignments = assignments.stream()
                .sorted(
                        Comparator.comparing(
                                AssignmentResponse::getDueDateTime,
                                Comparator.nullsLast(
                                        Comparator.naturalOrder()
                                )
                        )
                )
                .toList();

        StringBuilder response =
                new StringBuilder("Your assignments:\n");

        for (AssignmentResponse assignment : assignments) {

            response.append("• ")
                    .append(assignment.getTitle());

            if (assignment.getSubjectName() != null) {
                response.append(" | Subject: ")
                        .append(assignment.getSubjectName());
            }

            if (assignment.getDueDateTime() != null) {
                response.append(" | Due: ")
                        .append(assignment.getDueDateTime());
            }

            response.append("\n");
        }

        return response.toString().trim();
    }

    private String getSubjectAssignmentsResponse(
            String userEmail,
            UUID subjectId) {

        List<AssignmentResponse> assignments =
                assignmentService.getAssignments(userEmail);

        List<AssignmentResponse> subjectAssignments =
                assignments.stream()
                        .filter(assignment ->
                                subjectId.equals(
                                        assignment.getSubjectId()
                                ))
                        .sorted(
                                Comparator.comparing(
                                        AssignmentResponse::getDueDateTime,
                                        Comparator.nullsLast(
                                                Comparator.naturalOrder()
                                        )
                                )
                        )
                        .toList();

        if (subjectAssignments.isEmpty()) {
            return "No assignments found for that subject.";
        }

        String subjectName =
                subjectAssignments.get(0).getSubjectName();

        StringBuilder response =
                new StringBuilder(
                        "Assignments for "
                                + subjectName
                                + ":\n"
                );

        for (AssignmentResponse assignment :
                subjectAssignments) {

            response.append("• ")
                    .append(assignment.getTitle());

            if (assignment.getDueDateTime() != null) {
                response.append(" | Due: ")
                        .append(assignment.getDueDateTime());
            }

            response.append("\n");
        }

        return response.toString().trim();
    }

    private String getUpcomingAssignmentsResponse(
            String userEmail,
            UUID subjectId) {

        List<AssignmentResponse> assignments =
                assignmentService.getAssignments(userEmail);

        LocalDateTime now =
                LocalDateTime.now();

        LocalDateTime future =
                now.plusDays(7);

        List<AssignmentResponse> upcoming =
                assignments.stream()
                        .filter(assignment ->
                                assignment.getDueDateTime() != null)
                        .filter(assignment ->
                                !assignment.getDueDateTime()
                                        .isBefore(now))
                        .filter(assignment ->
                                !assignment.getDueDateTime()
                                        .isAfter(future))
                        .filter(assignment ->
                                subjectId == null
                                        || subjectId.equals(
                                        assignment.getSubjectId()
                                ))
                        .sorted(
                                Comparator.comparing(
                                        AssignmentResponse::getDueDateTime
                                )
                        )
                        .toList();

        if (upcoming.isEmpty()) {
            if (subjectId != null) {
                return "There are no assignments due in the next 7 days for that subject.";
            }

            return "There are no assignments due in the next 7 days.";
        }

        StringBuilder response =
                new StringBuilder(
                        "Assignments due in the next 7 days:\n"
                );

        for (AssignmentResponse assignment : upcoming) {

            response.append("• ")
                    .append(assignment.getTitle());

            if (assignment.getSubjectName() != null) {
                response.append(" | Subject: ")
                        .append(assignment.getSubjectName());
            }

            response.append(" | Due: ")
                    .append(assignment.getDueDateTime())
                    .append("\n");
        }

        return response.toString().trim();
    }

    // =============================================================
    // COURSE MATERIALS
    // =============================================================

    private String getMyCourseMaterialsResponse(
            String userEmail) {

        List<CourseMaterialResponse> materials =
                courseMaterialService.getMaterials(userEmail);

        if (materials == null || materials.isEmpty()) {
            return "No course materials are available.";
        }

        List<CourseMaterialResponse> sortedMaterials =
                materials.stream()
                        .sorted(
                                Comparator.comparing(
                                        CourseMaterialResponse::getCreatedAt,
                                        Comparator.nullsLast(
                                                Comparator.reverseOrder()
                                        )
                                )
                        )
                        .toList();

        StringBuilder response =
                new StringBuilder("Your course materials:\n");

        for (CourseMaterialResponse material :
                sortedMaterials) {

            response.append("• ")
                    .append(material.getTitle());

            if (material.getSubjectName() != null) {
                response.append(" | Subject: ")
                        .append(material.getSubjectName());
            }

            if (material.getAttachmentPath() != null
                    && !material.getAttachmentPath().isBlank()) {

                response.append(" | Attachment available");
            }

            response.append("\n");
        }

        return response.toString().trim();
    }

    private String getSubjectMaterialsResponse(
            String userEmail,
            UUID subjectId) {

        List<CourseMaterialResponse> materials =
                courseMaterialService.getMaterials(userEmail);

        List<CourseMaterialResponse> subjectMaterials =
                materials.stream()
                        .filter(material ->
                                subjectId.equals(
                                        material.getSubjectId()
                                ))
                        .sorted(
                                Comparator.comparing(
                                        CourseMaterialResponse::getCreatedAt,
                                        Comparator.nullsLast(
                                                Comparator.reverseOrder()
                                        )
                                )
                        )
                        .toList();

        if (subjectMaterials.isEmpty()) {
            return "No course materials found for that subject.";
        }

        String subjectName =
                subjectMaterials.get(0).getSubjectName();

        StringBuilder response =
                new StringBuilder(
                        "Course materials for "
                                + subjectName
                                + ":\n"
                );

        for (CourseMaterialResponse material :
                subjectMaterials) {

            response.append("• ")
                    .append(material.getTitle());

            if (material.getAttachmentPath() != null
                    && !material.getAttachmentPath().isBlank()) {

                response.append(" | Attachment available");
            }

            response.append("\n");
        }

        return response.toString().trim();
    }

    // =============================================================
    // ASSESSMENTS
    // =============================================================

    private String getMyAssessmentsResponse(
            String userEmail) {

        Student student =
                studentRepository.findByUserEmail(userEmail);

        if (student == null) {
            return "Student profile not found.";
        }

        if (student.getSection() == null) {
            return "You are not assigned to a section.";
        }

        List<AssessmentResponse> assessments =
                assessmentRepository.findBySectionId(
                        student.getSection().getId()
                ).stream()
                        .map(this::toAssessmentResponse)
                        .sorted(
                                Comparator.comparing(
                                        AssessmentResponse::getAssessmentDate,
                                        Comparator.nullsLast(
                                                Comparator.naturalOrder()
                                        )
                                )
                        )
                        .toList();

        if (assessments.isEmpty()) {
            return "No assessments found.";
        }

        StringBuilder response =
                new StringBuilder("Your assessments:\n");

        for (AssessmentResponse assessment :
                assessments) {

            response.append("• ")
                    .append(assessment.getTitle())
                    .append(" | ")
                    .append(assessment.getSubjectName());

            if (assessment.getType() != null) {
                response.append(" | Type: ")
                        .append(assessment.getType());
            }

            if (assessment.getAssessmentDate() != null) {
                response.append(" | Date: ")
                        .append(assessment.getAssessmentDate());
            }

            response.append("\n");
        }

        return response.toString().trim();
    }

    private String getSubjectAssessmentsResponse(
            String userEmail,
            UUID subjectId) {

        Student student =
                studentRepository.findByUserEmail(userEmail);

        if (student == null) {
            return "Student profile not found.";
        }

        if (student.getSection() == null) {
            return "You are not assigned to a section.";
        }

        List<AssessmentResponse> assessments =
                assessmentRepository
                        .findBySectionIdAndSubjectId(
                                student.getSection().getId(),
                                subjectId
                        )
                        .stream()
                        .map(this::toAssessmentResponse)
                        .sorted(
                                Comparator.comparing(
                                        AssessmentResponse::getAssessmentDate,
                                        Comparator.nullsLast(
                                                Comparator.naturalOrder()
                                        )
                                )
                        )
                        .toList();

        if (assessments.isEmpty()) {
            return "No assessments found for that subject.";
        }

        String subjectName =
                assessments.get(0).getSubjectName();

        StringBuilder response =
                new StringBuilder(
                        "Assessments for "
                                + subjectName
                                + ":\n"
                );

        for (AssessmentResponse assessment :
                assessments) {

            response.append("• ")
                    .append(assessment.getTitle());

            if (assessment.getType() != null) {
                response.append(" | Type: ")
                        .append(assessment.getType());
            }

            if (assessment.getMaximumMarks() != null) {
                response.append(" | Maximum marks: ")
                        .append(assessment.getMaximumMarks());
            }

            if (assessment.getAssessmentDate() != null) {
                response.append(" | Date: ")
                        .append(assessment.getAssessmentDate());
            }

            response.append("\n");
        }

        return response.toString().trim();
    }

    private AssessmentResponse toAssessmentResponse(
            com.campusconnect.model.Assessment assessment) {

        AssessmentResponse response =
                new AssessmentResponse();

        response.setId(assessment.getId());
        response.setTitle(assessment.getTitle());
        response.setDescription(assessment.getDescription());
        response.setType(assessment.getType());
        response.setMaximumMarks(
                assessment.getMaximumMarks()
        );
        response.setAssessmentDate(
                assessment.getAssessmentDate()
        );

        if (assessment.getSection() != null) {
            response.setSectionId(
                    assessment.getSection().getId()
            );
            response.setSectionName(
                    assessment.getSection().getName()
            );
        }

        if (assessment.getSubject() != null) {
            response.setSubjectId(
                    assessment.getSubject().getId()
            );
            response.setSubjectName(
                    assessment.getSubject().getName()
            );
            response.setSubjectCode(
                    assessment.getSubject().getCode()
            );
        }

        if (assessment.getTeacher() != null) {
            response.setTeacherId(
                    assessment.getTeacher().getId()
            );
            response.setTeacherEmail(
                    assessment.getTeacher().getEmail()
            );
        }

        response.setCreatedAt(
                assessment.getCreatedAt()
        );
        response.setUpdatedAt(
                assessment.getUpdatedAt()
        );

        return response;
    }

    // =============================================================
    // INTERNAL MARKS
    // =============================================================

    private String getMyInternalMarksResponse(
            String userEmail) {

        List<StudentMarkResponse> marks =
                studentMarkService.getMyMarks(userEmail);

        if (marks == null || marks.isEmpty()) {
            return "No internal assessment marks have been recorded yet.";
        }

        StringBuilder response =
                new StringBuilder("Your internal assessment marks:\n");

        for (StudentMarkResponse mark : marks) {

            response.append("• ")
                    .append(mark.getAssessmentTitle())
                    .append(" | ")
                    .append(mark.getMarks())
                    .append("/")
                    .append(mark.getMaximumMarks());

            if (mark.getMaximumMarks() != null
                    && mark.getMaximumMarks()
                    .compareTo(java.math.BigDecimal.ZERO) > 0) {

                response.append(" | ")
                        .append(
                                calculatePercentage(
                                        mark.getMarks(),
                                        mark.getMaximumMarks()
                                )
                        )
                        .append("%");
            }

            response.append("\n");
        }

        return response.toString().trim();
    }

    private String getSubjectInternalMarksResponse(
            String userEmail,
            UUID subjectId) {

        List<StudentMarkResponse> marks =
                studentMarkService.getMyMarks(userEmail);

        if (marks == null || marks.isEmpty()) {
            return "No internal assessment marks have been recorded yet.";
        }

        List<StudentMarkResponse> subjectMarks =
                marks.stream()
                        .filter(mark ->
                                isMarkForSubject(
                                        mark,
                                        subjectId
                                ))
                        .toList();

        if (subjectMarks.isEmpty()) {
            return "No internal assessment marks found for that subject.";
        }

        Subject subject =
                subjectRepository.findById(subjectId)
                        .orElse(null);

        String subjectName =
                subject != null
                        ? subject.getName()
                        : "Subject";

        StringBuilder response =
                new StringBuilder(
                        "Internal marks for "
                                + subjectName
                                + ":\n"
                );

        for (StudentMarkResponse mark : subjectMarks) {

            response.append("• ")
                    .append(mark.getAssessmentTitle())
                    .append(" | ")
                    .append(mark.getMarks())
                    .append("/")
                    .append(mark.getMaximumMarks())
                    .append("\n");
        }

        return response.toString().trim();
    }

    private boolean isMarkForSubject(
            StudentMarkResponse mark,
            UUID subjectId) {

        if (mark == null
                || mark.getAssessmentId() == null) {

            return false;
        }

        return assessmentRepository.findById(
                        mark.getAssessmentId()
                )
                .map(assessment ->
                        assessment.getSubject() != null
                                && subjectId.equals(
                                assessment.getSubject().getId()
                        ))
                .orElse(false);
    }

    // =============================================================
    // EXAM TIMETABLE
    // =============================================================

    private String getMyExamTimetableResponse(
            String userEmail) {

        List<ExamResponse> exams =
                studentExamService.getMyExamTimetable(
                        userEmail
                );

        if (exams == null || exams.isEmpty()) {
            return "You do not have any exams scheduled.";
        }

        StringBuilder response =
                new StringBuilder("Your exam timetable:\n");

        for (ExamResponse exam : exams) {

            response.append("• ")
                    .append(exam.getTitle())
                    .append(" | ")
                    .append(exam.getSubjectName())
                    .append(" | Date: ")
                    .append(exam.getExamDate())
                    .append(" | ")
                    .append(formatTime(exam.getStartTime()))
                    .append(" - ")
                    .append(formatTime(exam.getEndTime()))
                    .append("\n");
        }

        return response.toString().trim();
    }

    private String getSubjectExamTimetableResponse(
            UUID subjectId,
            String userEmail) {

        List<ExamResponse> exams =
                studentExamService.getMyExamTimetable(
                        userEmail
                );

        List<ExamResponse> subjectExams =
                exams.stream()
                        .filter(exam ->
                                subjectId.equals(
                                        exam.getSubjectId()
                                ))
                        .toList();

        if (subjectExams.isEmpty()) {
            return "No exam is scheduled for that subject.";
        }

        String subjectName =
                subjectExams.get(0).getSubjectName();

        StringBuilder response =
                new StringBuilder(
                        "Exam timetable for "
                                + subjectName
                                + ":\n"
                );

        for (ExamResponse exam : subjectExams) {

            response.append("• ")
                    .append(exam.getTitle())
                    .append(" | Date: ")
                    .append(exam.getExamDate())
                    .append(" | ")
                    .append(formatTime(exam.getStartTime()))
                    .append(" - ")
                    .append(formatTime(exam.getEndTime()))
                    .append("\n");
        }

        return response.toString().trim();
    }

    // =============================================================
    // EXAM MARKS
    // =============================================================

    private String getMyExamMarksResponse(
            String userEmail) {

        Student student =
                studentRepository.findByUserEmail(userEmail);

        if (student == null) {
            return "Student profile not found.";
        }

        var marks =
                examMarkRepository.findByStudentId(
                        student.getId()
                );

        if (marks == null || marks.isEmpty()) {
            return "No examination marks have been recorded yet.";
        }

        StringBuilder response =
                new StringBuilder("Your examination marks:\n");

        for (var mark : marks) {

            if (mark.getExam() == null) {
                continue;
            }

            response.append("• ")
                    .append(mark.getExam().getTitle())
                    .append(" | ")
                    .append(mark.getMarks())
                    .append("/")
                    .append(mark.getExam().getMaximumMarks());

            if (mark.getExam().getSubject() != null) {
                response.append(" | Subject: ")
                        .append(
                                mark.getExam()
                                        .getSubject()
                                        .getName()
                        );
            }

            response.append("\n");
        }

        return response.toString().trim();
    }

    private String getSubjectExamMarksResponse(
            UUID subjectId,
            String userEmail) {

        Student student =
                studentRepository.findByUserEmail(userEmail);

        if (student == null) {
            return "Student profile not found.";
        }

        var marks =
                examMarkRepository.findByStudentId(
                        student.getId()
                );

        List<?> subjectMarks =
                marks.stream()
                        .filter(mark ->
                                mark.getExam() != null
                                        && mark.getExam().getSubject() != null
                                        && subjectId.equals(
                                        mark.getExam()
                                                .getSubject()
                                                .getId()
                                ))
                        .toList();

        if (subjectMarks.isEmpty()) {
            return "No examination marks found for that subject.";
        }

        Subject subject =
                subjectRepository.findById(subjectId)
                        .orElse(null);

        String subjectName =
                subject != null
                        ? subject.getName()
                        : "Subject";

        StringBuilder response =
                new StringBuilder(
                        "Examination marks for "
                                + subjectName
                                + ":\n"
                );

        for (Object object : subjectMarks) {

            com.campusconnect.model.ExamMark mark =
                    (com.campusconnect.model.ExamMark) object;

            response.append("• ")
                    .append(mark.getExam().getTitle())
                    .append(" | ")
                    .append(mark.getMarks())
                    .append("/")
                    .append(mark.getExam().getMaximumMarks())
                    .append("\n");
        }

        return response.toString().trim();
    }

    // =============================================================
    // RESULTS
    // =============================================================

    private String getMyResultsResponse(
            String userEmail) {

        List<StudentExamResultResponse> results =
                studentExamResultService.getMyResults(
                        userEmail
                );

        if (results == null || results.isEmpty()) {
            return "No published examination results are available.";
        }

        StringBuilder response =
                new StringBuilder("Your published results:\n");

        for (StudentExamResultResponse result :
                results) {

            response.append("• ")
                    .append(result.getExamTitle())
                    .append(" | ")
                    .append(result.getSubjectName())
                    .append(" | ")
                    .append(result.getMarksObtained())
                    .append("/")
                    .append(result.getMaximumMarks())
                    .append(" | ")
                    .append(result.getPercentage())
                    .append("%\n");
        }

        return response.toString().trim();
    }

    private String getSubjectResultsResponse(
            UUID subjectId,
            String userEmail) {

        List<StudentExamResultResponse> results =
                studentExamResultService.getMyResults(
                        userEmail
                );

        List<StudentExamResultResponse> subjectResults =
                results.stream()
                        .filter(result ->
                                subjectId.equals(
                                        result.getSubjectId()
                                ))
                        .toList();

        if (subjectResults.isEmpty()) {
            return "No published result is available for that subject.";
        }

        String subjectName =
                subjectResults.get(0).getSubjectName();

        StringBuilder response =
                new StringBuilder(
                        "Published results for "
                                + subjectName
                                + ":\n"
                );

        for (StudentExamResultResponse result :
                subjectResults) {

            response.append("• ")
                    .append(result.getExamTitle())
                    .append(" | Marks: ")
                    .append(result.getMarksObtained())
                    .append("/")
                    .append(result.getMaximumMarks())
                    .append(" | Percentage: ")
                    .append(result.getPercentage())
                    .append("%\n");
        }

        return response.toString().trim();
    }

    // =============================================================
    // ACADEMIC PERFORMANCE
    // =============================================================

    private String getMyPerformanceResponse(
            String userEmail) {

        AcademicPerformanceResponse performance =
                academicPerformanceService.getMyPerformance(
                        userEmail
                );

        if (performance == null) {
            return "Academic performance information is not available.";
        }

        StringBuilder response =
                new StringBuilder(
                        "Your academic performance:\n"
                );

        response.append("• Overall marks: ")
                .append(performance.getTotalMarksObtained())
                .append("/")
                .append(performance.getTotalMaximumMarks())
                .append("\n");

        response.append("• Overall percentage: ")
                .append(performance.getOverallPercentage())
                .append("%\n");

        response.append("• Performance level: ")
                .append(performance.getPerformanceLevel())
                .append("\n");

        List<SubjectPerformanceResponse> subjects =
                performance.getSubjects();

        if (subjects != null && !subjects.isEmpty()) {

            response.append("\nSubject-wise performance:\n");

            for (SubjectPerformanceResponse subject :
                    subjects) {

                response.append("• ")
                        .append(subject.getSubjectName())
                        .append(" (")
                        .append(subject.getSubjectCode())
                        .append(")")
                        .append(": ")
                        .append(subject.getPercentage())
                        .append("%")
                        .append(" | ")
                        .append(subject.getPerformanceLevel())
                        .append("\n");
            }
        }

        return response.toString().trim();
    }

    // =============================================================
    // FACILITIES
    // =============================================================

    private String getFacilitiesResponse() {

        List<FacilityResponse> facilities =
                facilityService.getAllFacilities();

        if (facilities == null || facilities.isEmpty()) {
            return "There are no facilities available.";
        }

        StringBuilder response =
                new StringBuilder("Available facilities:\n");

        for (FacilityResponse facility : facilities) {

            response.append("• ")
                    .append(facility.getName())
                    .append(" | Type: ")
                    .append(facility.getType())
                    .append(" | Capacity: ")
                    .append(facility.getCapacity())
                    .append(" | Location: ")
                    .append(facility.getLocation())
                    .append("\n");
        }

        return response.toString().trim();
    }

    // =============================================================
    // BOOKINGS
    // =============================================================

    private String getMyBookingsResponse(
            String userEmail) {

        List<BookingResponse> bookings =
                bookingService.getMyBookings(userEmail);

        if (bookings == null || bookings.isEmpty()) {
            return "You do not have any bookings.";
        }

        StringBuilder response =
                new StringBuilder("Your bookings:\n");

        for (BookingResponse booking : bookings) {

            response.append("• Facility: ")
                    .append(booking.getFacilityName())
                    .append(" | Date: ")
                    .append(booking.getBookingDate())
                    .append(" | Time: ")
                    .append(booking.getStartTime())
                    .append(" - ")
                    .append(booking.getEndTime())
                    .append(" | Status: ")
                    .append(booking.getStatus())
                    .append("\n");
        }

        return response.toString().trim();
    }

    // =============================================================
    // BUS PASS
    // =============================================================

    private String getBusPassDetailsResponse(
            String userEmail) {

        User user =
                userRepository.findByEmail(userEmail)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "User not found"
                                )
                        );

        List<BusPassResponse> passes =
                busPassService.getStudentPasses(
                        user.getId()
                );

        if (passes == null || passes.isEmpty()) {
            return "You do not have any bus pass applications.";
        }

        StringBuilder response =
                new StringBuilder("Your bus passes:\n");

        for (BusPassResponse pass : passes) {

            response.append("• Status: ")
                    .append(pass.getStatus())
                    .append("\n")
                    .append("  Route: ")
                    .append(pass.getRoute())
                    .append("\n")
                    .append("  Start date: ")
                    .append(pass.getStartDate())
                    .append("\n")
                    .append("  Expiry date: ")
                    .append(pass.getExpiryDate())
                    .append("\n");
        }

        return response.toString().trim();
    }

    private String getBusPassStatusResponse(
            String userEmail) {

        User user =
                userRepository.findByEmail(userEmail)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "User not found"
                                )
                        );

        List<BusPassResponse> passes =
                busPassService.getStudentPasses(
                        user.getId()
                );

        if (passes == null || passes.isEmpty()) {
            return "You do not have any bus pass applications.";
        }

        StringBuilder response =
                new StringBuilder("Your bus pass status:\n");

        for (BusPassResponse pass : passes) {

            response.append("• Status: ")
                    .append(pass.getStatus())
                    .append(" | Route: ")
                    .append(pass.getRoute())
                    .append("\n");
        }

        return response.toString().trim();
    }

    private String getBusPassExpiryResponse(
            String userEmail) {

        User user =
                userRepository.findByEmail(userEmail)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "User not found"
                                )
                        );

        List<BusPassResponse> passes =
                busPassService.getStudentPasses(
                        user.getId()
                );

        if (passes == null || passes.isEmpty()) {
            return "You do not have any bus pass applications.";
        }

        StringBuilder response =
                new StringBuilder(
                        "Your bus pass expiry details:\n"
                );

        for (BusPassResponse pass : passes) {

            response.append("• Route: ")
                    .append(pass.getRoute())
                    .append(" | Status: ")
                    .append(pass.getStatus())
                    .append(" | Expires: ")
                    .append(pass.getExpiryDate())
                    .append("\n");
        }

        return response.toString().trim();
    }

    private String getBusPassRouteResponse(
            String userEmail) {

        User user =
                userRepository.findByEmail(userEmail)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "User not found"
                                )
                        );

        List<BusPassResponse> passes =
                busPassService.getStudentPasses(
                        user.getId()
                );

        if (passes == null || passes.isEmpty()) {
            return "You do not have any bus pass applications.";
        }

        StringBuilder response =
                new StringBuilder("Your bus pass routes:\n");

        for (BusPassResponse pass : passes) {

            response.append("• Route: ")
                    .append(pass.getRoute())
                    .append(" | Status: ")
                    .append(pass.getStatus())
                    .append("\n");
        }

        return response.toString().trim();
    }

    // =============================================================
    // NOTIFICATIONS
    // =============================================================

    private String getAllNotificationsResponse(
            String userEmail) {

        User user =
                userRepository.findByEmail(userEmail)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "User not found"
                                )
                        );

        List<Notification> notifications =
                notificationService.getUserNotifications(
                        user.getId()
                );

        if (notifications == null || notifications.isEmpty()) {
            return "You have no notifications.";
        }

        StringBuilder response =
                new StringBuilder("Your notifications:\n");

        for (Notification notification : notifications) {

            response.append("• ")
                    .append(notification.getTitle())
                    .append(" — ")
                    .append(notification.getMessage())
                    .append(" [")
                    .append(notification.getType())
                    .append("]");

            if (!notification.isRead()) {
                response.append(" — Unread");
            }

            response.append("\n");
        }

        return response.toString().trim();
    }

    private String getLatestNotificationsResponse(
            String userEmail) {

        User user =
                userRepository.findByEmail(userEmail)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "User not found"
                                )
                        );

        List<Notification> notifications =
                notificationService.getUserNotifications(
                        user.getId()
                );

        if (notifications == null || notifications.isEmpty()) {
            return "You have no notifications.";
        }

        StringBuilder response =
                new StringBuilder("Latest notifications:\n");

        int count = 0;

        for (Notification notification : notifications) {

            response.append("• ")
                    .append(notification.getTitle())
                    .append(" — ")
                    .append(notification.getMessage());

            if (!notification.isRead()) {
                response.append(" — Unread");
            }

            response.append("\n");

            count++;

            if (count >= 5) {
                break;
            }
        }

        return response.toString().trim();
    }

    private String getUnreadNotificationsResponse(
            String userEmail) {

        User user =
                userRepository.findByEmail(userEmail)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "User not found"
                                )
                        );

        List<Notification> notifications =
                notificationService.getUserNotifications(
                        user.getId()
                );

        List<Notification> unreadNotifications =
                notifications.stream()
                        .filter(
                                notification ->
                                        !notification.isRead()
                        )
                        .toList();

        if (unreadNotifications.isEmpty()) {
            return "You have no unread notifications.";
        }

        StringBuilder response =
                new StringBuilder(
                        "Your unread notifications:\n"
                );

        for (Notification notification :
                unreadNotifications) {

            response.append("• ")
                    .append(notification.getTitle())
                    .append(" — ")
                    .append(notification.getMessage())
                    .append("\n");
        }

        return response.toString().trim();
    }

    private String getUnreadNotificationCountResponse(
            String userEmail) {

        User user =
                userRepository.findByEmail(userEmail)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "User not found"
                                )
                        );

        long count =
                notificationService.getUnreadCount(
                        user.getId()
                );

        if (count == 0) {
            return "You have no unread notifications.";
        }

        return "You have " + count
                + " unread notification"
                + (count == 1 ? "." : "s.");
    }

    private String getTodayNotificationsResponse(
            String userEmail) {

        User user =
                userRepository.findByEmail(userEmail)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "User not found"
                                )
                        );

        List<Notification> notifications =
                notificationService.getUserNotifications(
                        user.getId()
                );

        LocalDate today =
                LocalDate.now();

        List<Notification> todayNotifications =
                notifications.stream()
                        .filter(
                                notification ->
                                        notification.getCreatedAt() != null
                                                && notification
                                                .getCreatedAt()
                                                .toLocalDate()
                                                .equals(today)
                        )
                        .toList();

        if (todayNotifications.isEmpty()) {
            return "You have no notifications for today.";
        }

        StringBuilder response =
                new StringBuilder(
                        "Today's notifications:\n"
                );

        for (Notification notification :
                todayNotifications) {

            response.append("• ")
                    .append(notification.getTitle())
                    .append(" — ")
                    .append(notification.getMessage());

            if (!notification.isRead()) {
                response.append(" — Unread");
            }

            response.append("\n");
        }

        return response.toString().trim();
    }

    // =============================================================
    // NOTICES
    // =============================================================

    private String getAllNoticesResponse(
            String userEmail) {

        List<NoticeResponse> notices =
                noticeService.getAllNotices(userEmail);

        if (notices == null || notices.isEmpty()) {
            return "No notices found.";
        }

        StringBuilder response =
                new StringBuilder("Your notices:\n");

        for (NoticeResponse notice : notices) {

            response.append("• ")
                    .append(notice.getTitle())
                    .append(" — ")
                    .append(notice.getDate());

            if (notice.getDescription() != null
                    && !notice.getDescription().isBlank()) {

                response.append("\n  ")
                        .append(notice.getDescription());
            }

            if (notice.getAttachmentPath() != null
                    && !notice.getAttachmentPath().isBlank()) {

                response.append("\n  Attachment available");
            }

            response.append("\n");
        }

        return response.toString().trim();
    }

    private String getLatestNoticesResponse(
            String userEmail) {

        List<NoticeResponse> notices =
                noticeService.getAllNotices(userEmail);

        if (notices == null || notices.isEmpty()) {
            return "No notices found.";
        }

        notices = notices.stream()
                .sorted(
                        (a, b) ->
                                b.getDate().compareTo(
                                        a.getDate()
                                )
                )
                .toList();

        StringBuilder response =
                new StringBuilder("Latest notices:\n");

        int count = 0;

        for (NoticeResponse notice : notices) {

            response.append("• ")
                    .append(notice.getTitle())
                    .append(" — ")
                    .append(notice.getDate());

            if (notice.getDescription() != null
                    && !notice.getDescription().isBlank()) {

                response.append("\n  ")
                        .append(notice.getDescription());
            }

            if (notice.getAttachmentPath() != null
                    && !notice.getAttachmentPath().isBlank()) {

                response.append("\n  Attachment available");
            }

            response.append("\n");

            count++;

            if (count >= 5) {
                break;
            }
        }

        return response.toString().trim();
    }

    private String getTodayNoticesResponse(
            String userEmail) {

        List<NoticeResponse> notices =
                noticeService.getAllNotices(userEmail);

        if (notices == null || notices.isEmpty()) {
            return "No notices found.";
        }

        LocalDate today =
                LocalDate.now();

        List<NoticeResponse> todayNotices =
                notices.stream()
                        .filter(
                                notice ->
                                        notice.getDate() != null
                                                && notice.getDate()
                                                .equals(today)
                        )
                        .toList();

        if (todayNotices.isEmpty()) {
            return "There are no notices for today.";
        }

        StringBuilder response =
                new StringBuilder(
                        "Today's notices:\n"
                );

        for (NoticeResponse notice :
                todayNotices) {

            response.append("• ")
                    .append(notice.getTitle());

            if (notice.getDescription() != null
                    && !notice.getDescription().isBlank()) {

                response.append("\n  ")
                        .append(notice.getDescription());
            }

            if (notice.getAttachmentPath() != null
                    && !notice.getAttachmentPath().isBlank()) {

                response.append("\n  Attachment available");
            }

            response.append("\n");
        }

        return response.toString().trim();
    }

    // =============================================================
    // ATTENDANCE
    // =============================================================

    private String getSubjectAttendanceResponse(
            UUID subjectId,
            String userEmail) {

        Student student =
                studentRepository.findByUserEmail(
                        userEmail
                );

        if (student == null) {
            return "Student profile not found.";
        }

        AttendanceSummaryResponse summary =
                attendanceService.getSubjectAttendanceSummary(
                        student.getId(),
                        subjectId,
                        userEmail
                );

        return String.format(
                "Your attendance is %.2f%%. "
                        + "You have attended %d out of %d classes.",
                summary.getAttendancePercentage(),
                summary.getPresentClasses(),
                summary.getTotalClasses()
        );
    }

    private String getAllSubjectAttendanceResponse(
            String userEmail) {

        Student student =
                studentRepository.findByUserEmail(
                        userEmail
                );

        if (student == null) {
            return "Student profile not found.";
        }

        var attendanceList =
                attendanceService.getAllSubjectAttendance(
                        student.getId(),
                        userEmail
                );

        if (attendanceList == null
                || attendanceList.isEmpty()) {

            return "No attendance records found.";
        }

        StringBuilder response =
                new StringBuilder("Your attendance:\n");

        for (var attendance : attendanceList) {

            response.append("• ")
                    .append(attendance.getSubjectName())
                    .append(": ")
                    .append(
                            String.format(
                                    "%.2f%%",
                                    attendance
                                            .getAttendancePercentage()
                            )
                    )
                    .append("\n");
        }

        return response.toString().trim();
    }

    private String getSubjectAttendanceRiskResponse(
            UUID subjectId,
            String userEmail) {

        Student student =
                studentRepository.findByUserEmail(
                        userEmail
                );

        if (student == null) {
            return "Student profile not found.";
        }

        var risk =
                attendanceService.getSubjectAttendanceRisk(
                        student.getId(),
                        subjectId,
                        userEmail
                );

        return String.format(
                "Your attendance is %.2f%%. Risk level: %s. "
                        + "You have attended %d out of %d classes and "
                        + "were absent for %d classes. "
                        + "Classes needed for 75%%: %d.",
                risk.getAttendancePercentage(),
                risk.getRiskLevel(),
                risk.getPresentClasses(),
                risk.getTotalClasses(),
                risk.getAbsentClasses(),
                risk.getClassesNeededFor75()
        );
    }

    private String getClassesNeededFor75Response(
            UUID subjectId,
            String userEmail) {

        Student student =
                studentRepository.findByUserEmail(
                        userEmail
                );

        if (student == null) {
            return "Student profile not found.";
        }

        var risk =
                attendanceService.getSubjectAttendanceRisk(
                        student.getId(),
                        subjectId,
                        userEmail
                );

        return String.format(
                "Your current attendance is %.2f%%. "
                        + "You need to attend %d more classes "
                        + "to reach 75%% attendance.",
                risk.getAttendancePercentage(),
                risk.getClassesNeededFor75()
        );
    }

    private String getSubjectAttendanceForecastResponse(
            UUID subjectId,
            String userEmail) {

        Student student =
                studentRepository.findByUserEmail(
                        userEmail
                );

        if (student == null) {
            return "Student profile not found.";
        }

        long defaultUpcomingClasses = 5L;

        var forecast =
                attendanceService.getSubjectAttendanceForecast(
                        student.getId(),
                        subjectId,
                        defaultUpcomingClasses,
                        userEmail
                );

        return String.format(
                "Current attendance: %.2f%%. "
                        + "For the next %d classes, "
                        + "your best-case attendance would be %.2f%% "
                        + "and worst-case attendance would be %.2f%%.",
                forecast.getCurrentPercentage(),
                forecast.getUpcomingClasses(),
                forecast.getBestCasePercentage(),
                forecast.getWorstCasePercentage()
        );
    }

    // =============================================================
    // TIMETABLE
    // =============================================================

    private String getTodayTimetableResponse(
            String userEmail) {

        Student student =
                studentRepository.findByUserEmail(
                        userEmail
                );

        if (student == null) {
            return "Student profile not found.";
        }

        if (student.getSection() == null) {
            return "You are not assigned to a section.";
        }

        List<TimetableResponse> timetable =
                timetableService.getTodayTimetable(
                        student.getSection().getId(),
                        userEmail
                );

        return formatTimetableResponse(
                timetable,
                "Today's timetable"
        );
    }

    private String getTomorrowTimetableResponse(
            String userEmail) {

        Student student =
                studentRepository.findByUserEmail(
                        userEmail
                );

        if (student == null) {
            return "Student profile not found.";
        }

        if (student.getSection() == null) {
            return "You are not assigned to a section.";
        }

        List<TimetableResponse> timetable =
                timetableService.getTomorrowTimetable(
                        student.getSection().getId(),
                        userEmail
                );

        return formatTimetableResponse(
                timetable,
                "Tomorrow's timetable"
        );
    }

    private String getWeeklyTimetableResponse(
            String userEmail) {

        Student student =
                studentRepository.findByUserEmail(
                        userEmail
                );

        if (student == null) {
            return "Student profile not found.";
        }

        if (student.getSection() == null) {
            return "You are not assigned to a section.";
        }

        List<TimetableResponse> timetable =
                timetableService.getWeeklyTimetable(
                        student.getSection().getId(),
                        userEmail
                );

        return formatTimetableResponse(
                timetable,
                "Your weekly timetable"
        );
    }

    private String getSubjectTimetableResponse(
            UUID subjectId,
            String userEmail) {

        Student student =
                studentRepository.findByUserEmail(
                        userEmail
                );

        if (student == null) {
            return "Student profile not found.";
        }

        if (student.getSection() == null) {
            return "You are not assigned to a section.";
        }

        List<TimetableResponse> timetable =
                timetableService.getWeeklyTimetable(
                        student.getSection().getId(),
                        userEmail
                );

        List<TimetableResponse> subjectTimetable =
                timetable.stream()
                        .filter(
                                item ->
                                        subjectId.equals(
                                                item.getSubjectId()
                                        )
                        )
                        .toList();

        if (subjectTimetable.isEmpty()) {
            return "No timetable entries found for that subject.";
        }

        Subject subject =
                subjectRepository.findById(subjectId)
                        .orElse(null);

        String subjectName =
                subject != null
                        ? subject.getName()
                        : "Subject";

        StringBuilder response =
                new StringBuilder(
                        "Timetable for "
                                + subjectName
                                + ":\n"
                );

        for (TimetableResponse item :
                subjectTimetable) {

            response.append("• ")
                    .append(item.getDay())
                    .append(" | ")
                    .append(formatTime(item.getStartTime()))
                    .append(" - ")
                    .append(formatTime(item.getEndTime()))
                    .append(" | Room: ")
                    .append(item.getRoom())
                    .append("\n");
        }

        return response.toString().trim();
    }

    private String formatTimetableResponse(
            List<TimetableResponse> timetable,
            String title) {

        if (timetable == null || timetable.isEmpty()) {
            return "No timetable classes found.";
        }

        StringBuilder response =
                new StringBuilder(title + ":\n");

        for (TimetableResponse item : timetable) {

            Subject subject =
                    subjectRepository.findById(
                            item.getSubjectId()
                    ).orElse(null);

            String subjectName =
                    subject != null
                            ? subject.getName()
                            : "Unknown subject";

            response.append("• ")
                    .append(item.getDay())
                    .append(" | ")
                    .append(subjectName)
                    .append(" | ")
                    .append(formatTime(item.getStartTime()))
                    .append(" - ")
                    .append(formatTime(item.getEndTime()))
                    .append(" | Room: ")
                    .append(item.getRoom())
                    .append("\n");
        }

        return response.toString().trim();
    }

    private String formatTime(LocalTime time) {

        if (time == null) {
            return "";
        }

        return time.toString();
    }

    // =============================================================
    // SUBJECTS
    // =============================================================

    private String getMySubjectsResponse(
            String userEmail) {

        Student student =
                studentRepository.findByUserEmail(
                        userEmail
                );

        if (student == null) {
            return "Student profile not found.";
        }

        if (student.getSection() == null) {
            return "You are not assigned to a section.";
        }

        List<Subject> subjects =
                subjectRepository.findBySectionId(
                        student.getSection().getId()
                );

        if (subjects == null || subjects.isEmpty()) {
            return "No subjects found for your section.";
        }

        StringBuilder response =
                new StringBuilder("Your subjects:\n");

        for (Subject subject : subjects) {

            response.append("• ")
                    .append(subject.getCode())
                    .append(" - ")
                    .append(subject.getName())
                    .append("\n");
        }

        return response.toString().trim();
    }

    private String getSubjectDetailsResponse(
            Subject subject,
            String userEmail) {

        Student student =
                studentRepository.findByUserEmail(
                        userEmail
                );

        if (student == null) {
            return "Student profile not found.";
        }

        if (student.getSection() == null) {
            return "You are not assigned to a section.";
        }

        if (subject.getSection() == null
                || !subject.getSection().getId()
                .equals(
                        student.getSection().getId()
                )) {

            return "That subject is not part of your section.";
        }

        return "Subject details:\n"
                + "• Code: " + subject.getCode() + "\n"
                + "• Name: " + subject.getName() + "\n"
                + "• Section: "
                + student.getSection().getName();
    }

    // =============================================================
    // UTILITY
    // =============================================================

    private java.math.BigDecimal calculatePercentage(
            java.math.BigDecimal obtained,
            java.math.BigDecimal maximum) {

        if (obtained == null
                || maximum == null
                || maximum.compareTo(
                        java.math.BigDecimal.ZERO
                ) == 0) {

            return java.math.BigDecimal.ZERO;
        }

        return obtained
                .multiply(
                        java.math.BigDecimal.valueOf(100)
                )
                .divide(
                        maximum,
                        2,
                        java.math.RoundingMode.HALF_UP
                );
    }
}