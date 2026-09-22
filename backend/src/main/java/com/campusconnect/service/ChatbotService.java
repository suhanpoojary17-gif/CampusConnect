package com.campusconnect.service;

import com.campusconnect.dto.AttendanceSummaryResponse;
import com.campusconnect.dto.BookingResponse;
import com.campusconnect.dto.BusPassResponse;
import com.campusconnect.dto.FacilityResponse;
import com.campusconnect.dto.NoticeResponse;
import com.campusconnect.dto.TimetableResponse;
import com.campusconnect.entity.Notification;
import com.campusconnect.entity.User;
import com.campusconnect.model.Student;
import com.campusconnect.model.Subject;
import com.campusconnect.repository.StudentRepository;
import com.campusconnect.repository.SubjectRepository;
import com.campusconnect.repository.UserRepository;

import java.time.LocalDate;
import java.time.LocalTime;
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
            BookingService bookingService) {

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
    }

    public String processMessage(String message, String userEmail) {

        if (message == null || message.trim().isEmpty()) {
            return "Please enter a message.";
        }

        String input = message.trim()
                .toLowerCase()
                .replaceAll("[?!.,]+$", "")
                .trim();

        // Greeting
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

        // Thanks
        if (input.equals("thanks")
                || input.equals("thank you")
                || input.equals("thankyou")
                || input.equals("thanks a lot")
                || input.equals("thank you so much")) {

            return "You're welcome! I'm happy to help.";
        }

        // Goodbye
        if (input.equals("bye")
                || input.equals("goodbye")
                || input.equals("see you")
                || input.equals("see you later")) {

            return "Goodbye! Have a great day.";
        }

        // Help
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
                    + "• Notices and announcements\n"
                    + "• Notifications and unread notifications\n"
                    + "• Bus pass details, status, route and expiry\n"
                    + "• Available facilities\n"
                    + "• Your facility bookings\n\n"
                    + "Examples:\n"
                    + "• What is my attendance in DBMS?\n"
                    + "• Show my timetable today\n"
                    + "• What subjects do I have?\n"
                    + "• Show my latest notices\n"
                    + "• How many unread notifications do I have?\n"
                    + "• Is my bus pass active?";
        }

        // Timetable
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
                    findSubjectFromMessage(input);

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

        // Notifications
        if (input.contains("notification")
                || input.contains("notifications")
                || input.contains("alert")
                || input.contains("alerts")) {

            // Unread / new notifications
            if (input.contains("unread")
                    || input.contains("new")
                    || input.contains("not read")) {

                return getUnreadNotificationsResponse(userEmail);
            }

            // Unread count
            if (input.contains("how many")
                    || input.contains("count")) {

                return getUnreadNotificationCountResponse(userEmail);
            }

            // Today's notifications
            if (input.contains("today")) {

                return getTodayNotificationsResponse(userEmail);
            }

            // Latest notifications
            if (input.contains("latest")
                    || input.contains("recent")) {

                return getLatestNotificationsResponse(userEmail);
            }

            return getAllNotificationsResponse(userEmail);
        }

        // Notices
        if (input.contains("notice")
                || input.contains("notices")
                || input.contains("announcement")
                || input.contains("announcements")) {

            // Today's notices
            if (input.contains("today")) {

                return getTodayNoticesResponse(userEmail);
            }

            // Latest / new notices
            if (input.contains("latest")
                    || input.contains("new")
                    || input.contains("recent")) {

                return getLatestNoticesResponse(userEmail);
            }

            return getAllNoticesResponse(userEmail);
        }

        // Bus Pass
        if (input.contains("bus pass")
                || input.contains("buspass")
                || input.contains("bus-pass")) {

            // Status
            if (input.contains("status")
                    || input.contains("active")
                    || input.contains("approved")
                    || input.contains("valid")
                    || input.contains("pending")
                    || input.contains("expired")) {

                return getBusPassStatusResponse(userEmail);
            }

            // Expiry
            if (input.contains("expire")
                    || input.contains("expiry")
                    || input.contains("expires")) {

                return getBusPassExpiryResponse(userEmail);
            }

            // Route
            if (input.contains("route")) {

                return getBusPassRouteResponse(userEmail);
            }

            return getBusPassDetailsResponse(userEmail);
        }

        // Facilities and Bookings
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

        // Subjects
        if (input.contains("subject")
                || input.contains("subjects")) {

            Optional<Subject> subjectOptional =
                    findSubjectFromMessage(input);

            if (subjectOptional.isPresent()) {

                Subject subject = subjectOptional.get();

                return getSubjectDetailsResponse(
                        subject,
                        userEmail
                );
            }

            return getMySubjectsResponse(userEmail);
        }

        // Attendance
        if (input.contains("attendance")
                || input.contains("risk")
                || input.contains("at risk")
                || input.contains("safe")
                || input.contains("how many classes")
                || input.contains("forecast")
                || input.contains("future")
                || input.contains("next classes")) {

            // Attendance forecast
            if (input.contains("forecast")
                    || input.contains("future")
                    || input.contains("next classes")) {

                Optional<Subject> subjectOptional =
                        findSubjectFromMessage(input);

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
                        findSubjectFromMessage(input);

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
                        findSubjectFromMessage(input);

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
                    findSubjectFromMessage(input);

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

        // Unknown question
        return "I'm not sure how to answer that yet.\n"
                + "I can help with attendance, timetable, subjects, "
                + "notices, notifications, bus passes, facilities and bookings.\n"
                + "Type 'help' to see some example questions.";
    }

    private Optional<Subject> findSubjectFromMessage(String input) {

        // Search by subject code
        for (Subject subject : subjectRepository.findAll()) {

            if (subject.getCode() != null
                    && input.contains(
                            subject.getCode().toLowerCase())) {

                return Optional.of(subject);
            }
        }

        // Search by subject name
        for (Subject subject : subjectRepository.findAll()) {

            if (subject.getName() != null
                    && input.contains(
                            subject.getName().toLowerCase())) {

                return Optional.of(subject);
            }
        }

        return Optional.empty();
    }

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

    private String getMyBookingsResponse(String userEmail) {

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

    private String getBusPassDetailsResponse(String userEmail) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<BusPassResponse> passes =
                busPassService.getStudentPasses(user.getId());

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

    private String getBusPassStatusResponse(String userEmail) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<BusPassResponse> passes =
                busPassService.getStudentPasses(user.getId());

        if (passes == null || passes.isEmpty()) {
            return "You do not have any bus pass applications.";
        }

        StringBuilder response =
                new StringBuilder("Your bus pass status:\n");

        for (BusPassResponse pass : passes) {

            response.append("• ")
                    .append("Status: ")
                    .append(pass.getStatus())
                    .append(" | Route: ")
                    .append(pass.getRoute())
                    .append("\n");
        }

        return response.toString().trim();
    }

    private String getBusPassExpiryResponse(String userEmail) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<BusPassResponse> passes =
                busPassService.getStudentPasses(user.getId());

        if (passes == null || passes.isEmpty()) {
            return "You do not have any bus pass applications.";
        }

        StringBuilder response =
                new StringBuilder("Your bus pass expiry details:\n");

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

    private String getBusPassRouteResponse(String userEmail) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<BusPassResponse> passes =
                busPassService.getStudentPasses(user.getId());

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

    private String getAllNotificationsResponse(String userEmail) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Notification> notifications =
                notificationService.getUserNotifications(user.getId());

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

    private String getLatestNotificationsResponse(String userEmail) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Notification> notifications =
                notificationService.getUserNotifications(user.getId());

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

    private String getUnreadNotificationsResponse(String userEmail) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Notification> notifications =
                notificationService.getUserNotifications(user.getId());

        List<Notification> unreadNotifications =
                notifications.stream()
                        .filter(notification -> !notification.isRead())
                        .toList();

        if (unreadNotifications.isEmpty()) {
            return "You have no unread notifications.";
        }

        StringBuilder response =
                new StringBuilder("Your unread notifications:\n");

        for (Notification notification : unreadNotifications) {

            response.append("• ")
                    .append(notification.getTitle())
                    .append(" — ")
                    .append(notification.getMessage())
                    .append("\n");
        }

        return response.toString().trim();
    }

    private String getUnreadNotificationCountResponse(String userEmail) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        long count =
                notificationService.getUnreadCount(user.getId());

        if (count == 0) {
            return "You have no unread notifications.";
        }

        return "You have " + count + " unread notification"
                + (count == 1 ? "." : "s.");
    }

    private String getTodayNotificationsResponse(String userEmail) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Notification> notifications =
                notificationService.getUserNotifications(user.getId());

        LocalDate today = LocalDate.now();

        List<Notification> todayNotifications =
                notifications.stream()
                        .filter(notification ->
                                notification.getCreatedAt() != null
                                        && notification.getCreatedAt()
                                        .toLocalDate()
                                        .equals(today))
                        .toList();

        if (todayNotifications.isEmpty()) {
            return "You have no notifications for today.";
        }

        StringBuilder response =
                new StringBuilder("Today's notifications:\n");

        for (Notification notification : todayNotifications) {

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

    private String getAllNoticesResponse(String userEmail) {

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

    private String getLatestNoticesResponse(String userEmail) {

        List<NoticeResponse> notices =
                noticeService.getAllNotices(userEmail);

        if (notices == null || notices.isEmpty()) {
            return "No notices found.";
        }

        notices = notices.stream()
                .sorted(
                        (a, b) ->
                                b.getDate().compareTo(a.getDate())
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

    private String getTodayNoticesResponse(String userEmail) {

        List<NoticeResponse> notices =
                noticeService.getAllNotices(userEmail);

        if (notices == null || notices.isEmpty()) {
            return "No notices found.";
        }

        LocalDate today = LocalDate.now();

        List<NoticeResponse> todayNotices =
                notices.stream()
                        .filter(notice ->
                                notice.getDate() != null
                                        && notice.getDate().equals(today))
                        .toList();

        if (todayNotices.isEmpty()) {
            return "There are no notices for today.";
        }

        StringBuilder response =
                new StringBuilder("Today's notices:\n");

        for (NoticeResponse notice : todayNotices) {

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

    private String getSubjectAttendanceResponse(
            UUID subjectId,
            String userEmail) {

        Student student =
                studentRepository.findByUserEmail(userEmail);

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
                "Your attendance is %.2f%%. You have attended %d out of %d classes.",
                summary.getAttendancePercentage(),
                summary.getPresentClasses(),
                summary.getTotalClasses()
        );
    }

    private String getAllSubjectAttendanceResponse(
            String userEmail) {

        Student student =
                studentRepository.findByUserEmail(userEmail);

        if (student == null) {
            return "Student profile not found.";
        }

        var attendanceList =
                attendanceService.getAllSubjectAttendance(
                        student.getId(),
                        userEmail
                );

        if (attendanceList == null || attendanceList.isEmpty()) {
            return "No attendance records found.";
        }

        StringBuilder response =
                new StringBuilder("Your attendance:\n");

        for (var attendance : attendanceList) {

            response.append("• ")
                    .append(attendance.getSubjectName())
                    .append(": ")
                    .append(String.format(
                            "%.2f%%",
                            attendance.getAttendancePercentage()
                    ))
                    .append("\n");
        }

        return response.toString().trim();
    }

    private String getSubjectAttendanceRiskResponse(
            UUID subjectId,
            String userEmail) {

        Student student =
                studentRepository.findByUserEmail(userEmail);

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
                studentRepository.findByUserEmail(userEmail);

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
                        + "You need to attend %d more classes to reach 75%% attendance.",
                risk.getAttendancePercentage(),
                risk.getClassesNeededFor75()
        );
    }

    private String getSubjectAttendanceForecastResponse(
            UUID subjectId,
            String userEmail) {

        Student student =
                studentRepository.findByUserEmail(userEmail);

        if (student == null) {
            return "Student profile not found.";
        }

        // Default to forecasting for the next 5 upcoming classes
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

    private String getTodayTimetableResponse(
            String userEmail) {

        Student student =
                studentRepository.findByUserEmail(userEmail);

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
                studentRepository.findByUserEmail(userEmail);

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
                studentRepository.findByUserEmail(userEmail);

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
                studentRepository.findByUserEmail(userEmail);

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
                        .filter(item ->
                                subjectId.equals(item.getSubjectId()))
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
                        "Timetable for " + subjectName + ":\n"
                );

        for (TimetableResponse item : subjectTimetable) {

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
    
    private String getMySubjectsResponse(String userEmail) {

        Student student =
                studentRepository.findByUserEmail(userEmail);

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
                studentRepository.findByUserEmail(userEmail);

        if (student == null) {
            return "Student profile not found.";
        }

        if (student.getSection() == null) {
            return "You are not assigned to a section.";
        }

        if (subject.getSection() == null
                || !subject.getSection().getId()
                        .equals(student.getSection().getId())) {

            return "That subject is not part of your section.";
        }

        return "Subject details:\n"
                + "• Code: " + subject.getCode() + "\n"
                + "• Name: " + subject.getName() + "\n"
                + "• Section: "
                + student.getSection().getName();
    }

}