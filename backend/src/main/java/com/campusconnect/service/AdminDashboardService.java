package com.campusconnect.service;

import com.campusconnect.dto.AdminDashboardActivityResponse;
import com.campusconnect.dto.AdminDashboardResponse;
import com.campusconnect.entity.Booking;
import com.campusconnect.entity.Notification;
import com.campusconnect.model.Assignment;
import com.campusconnect.model.Exam;
import com.campusconnect.model.ExamResultStatus;
import com.campusconnect.model.Notice;
import com.campusconnect.repository.AssessmentRepository;
import com.campusconnect.repository.AssignmentRepository;
import com.campusconnect.repository.BookingRepository;
import com.campusconnect.repository.CourseMaterialRepository;
import com.campusconnect.repository.DepartmentRepository;
import com.campusconnect.repository.ExamMarkRepository;
import com.campusconnect.repository.ExamRepository;
import com.campusconnect.repository.FacilityRepository;
import com.campusconnect.repository.NoticeRepository;
import com.campusconnect.repository.NotificationRepository;
import com.campusconnect.repository.SectionRepository;
import com.campusconnect.repository.SemesterRepository;
import com.campusconnect.repository.StudentRepository;
import com.campusconnect.repository.SubjectRepository;
import com.campusconnect.repository.TeacherRepository;
import com.campusconnect.repository.UserRepository;
import com.campusconnect.repository.YearRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
public class AdminDashboardService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;

    private final DepartmentRepository departmentRepository;
    private final YearRepository yearRepository;
    private final SemesterRepository semesterRepository;
    private final SectionRepository sectionRepository;
    private final SubjectRepository subjectRepository;

    private final AssignmentRepository assignmentRepository;
    private final CourseMaterialRepository courseMaterialRepository;
    private final AssessmentRepository assessmentRepository;

    private final ExamRepository examRepository;
    private final ExamMarkRepository examMarkRepository;

    private final NoticeRepository noticeRepository;
    private final NotificationRepository notificationRepository;

    private final FacilityRepository facilityRepository;
    private final BookingRepository bookingRepository;

    public AdminDashboardService(
            UserRepository userRepository,
            StudentRepository studentRepository,
            TeacherRepository teacherRepository,
            DepartmentRepository departmentRepository,
            YearRepository yearRepository,
            SemesterRepository semesterRepository,
            SectionRepository sectionRepository,
            SubjectRepository subjectRepository,
            AssignmentRepository assignmentRepository,
            CourseMaterialRepository courseMaterialRepository,
            AssessmentRepository assessmentRepository,
            ExamRepository examRepository,
            ExamMarkRepository examMarkRepository,
            NoticeRepository noticeRepository,
            NotificationRepository notificationRepository,
            FacilityRepository facilityRepository,
            BookingRepository bookingRepository) {

        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.teacherRepository = teacherRepository;

        this.departmentRepository = departmentRepository;
        this.yearRepository = yearRepository;
        this.semesterRepository = semesterRepository;
        this.sectionRepository = sectionRepository;
        this.subjectRepository = subjectRepository;

        this.assignmentRepository = assignmentRepository;
        this.courseMaterialRepository = courseMaterialRepository;
        this.assessmentRepository = assessmentRepository;

        this.examRepository = examRepository;
        this.examMarkRepository = examMarkRepository;

        this.noticeRepository = noticeRepository;
        this.notificationRepository = notificationRepository;

        this.facilityRepository = facilityRepository;
        this.bookingRepository = bookingRepository;
    }

    public AdminDashboardResponse getDashboard() {

        long totalUsers = userRepository.count();
        long totalStudents = studentRepository.count();
        long totalTeachers = teacherRepository.count();

        long totalDepartments = departmentRepository.count();
        long totalAcademicYears = yearRepository.count();
        long totalSemesters = semesterRepository.count();
        long totalSections = sectionRepository.count();
        long totalSubjects = subjectRepository.count();

        long totalAssignments = assignmentRepository.count();
        long totalCourseMaterials = courseMaterialRepository.count();
        long totalAssessments = assessmentRepository.count();

        long totalExams = examRepository.count();
        long totalExamMarks = examMarkRepository.count();

        long totalPublishedResults =
                examRepository.countByResultStatus(ExamResultStatus.PUBLISHED);

        long totalNotices = noticeRepository.count();
        long totalNotifications = notificationRepository.count();

        long totalFacilities = facilityRepository.count();
        long totalBookings = bookingRepository.count();

        return new AdminDashboardResponse(
                totalUsers,
                totalStudents,
                totalTeachers,
                totalDepartments,
                totalAcademicYears,
                totalSemesters,
                totalSections,
                totalSubjects,
                totalAssignments,
                totalCourseMaterials,
                totalAssessments,
                totalExams,
                totalExamMarks,
                totalPublishedResults,
                totalNotices,
                totalNotifications,
                totalFacilities,
                totalBookings
        );
    }

    public AdminDashboardActivityResponse getActivity() {

        LocalDateTime now = LocalDateTime.now();

        // 1. Fetch upcoming assignments (next 7 days)
        List<Assignment> upcomingAssignments =
                assignmentRepository.findByDueDateTimeBetween(
                        now,
                        now.plusDays(7)
                );

        upcomingAssignments.sort(
                Comparator.comparing(Assignment::getDueDateTime)
        );

        List<AdminDashboardActivityResponse.AssignmentActivityResponse>
                assignmentResponses =
                upcomingAssignments.stream()
                        .limit(10)
                        .map(assignment ->
                                new AdminDashboardActivityResponse
                                        .AssignmentActivityResponse(
                                        assignment.getId().toString(),
                                        assignment.getTitle(),
                                        assignment.getSubject().getName(),
                                        assignment.getSection().getName(),
                                        assignment.getDueDateTime().toString()
                                )
                        )
                        .toList();

        // 2. Fetch notices
        List<Notice> notices = noticeRepository.findAll();

        notices.sort(
                Comparator.comparing(
                        Notice::getDate,
                        Comparator.reverseOrder()
                )
        );

        List<AdminDashboardActivityResponse.NoticeActivityResponse>
                noticeResponses =
                notices.stream()
                        .limit(10)
                        .map(notice ->
                                new AdminDashboardActivityResponse
                                        .NoticeActivityResponse(
                                        notice.getId().toString(),
                                        notice.getTitle(),
                                        notice.getDescription(),
                                        notice.getDate().toString(),
                                        notice.getTargetSection().getName()
                                )
                        )
                        .toList();

        // 3. Fetch upcoming exams
        List<Exam> exams = examRepository.findAll();
        exams.removeIf(
                exam -> exam.getExamDate().isBefore(LocalDate.now())
        );
        exams.sort(
                Comparator.comparing(Exam::getExamDate)
                        .thenComparing(Exam::getStartTime)
        );

        List<AdminDashboardActivityResponse.ExamActivityResponse>
                examResponses =
                exams.stream()
                        .limit(10)
                        .map(exam ->
                                new AdminDashboardActivityResponse
                                        .ExamActivityResponse(
                                        exam.getId().toString(),
                                        exam.getTitle(),
                                        exam.getSubject().getName(),
                                        exam.getSection().getName(),
                                        exam.getExamDate().toString(),
                                        exam.getStartTime().toString(),
                                        exam.getEndTime().toString(),
                                        exam.getResultStatus().toString()
                                )
                        )
                        .toList();

        // 4. Fetch published exam results
        List<Exam> publishedExams = examRepository.findAll();

        publishedExams.removeIf(
                exam -> exam.getResultStatus() != ExamResultStatus.PUBLISHED
        );

        publishedExams.sort(
                Comparator.comparing(
                        Exam::getExamDate,
                        Comparator.reverseOrder()
                )
        );

        List<AdminDashboardActivityResponse.PublishedResultActivityResponse>
                publishedResultResponses =
                publishedExams.stream()
                        .limit(10)
                        .map(exam ->
                                new AdminDashboardActivityResponse
                                        .PublishedResultActivityResponse(
                                        exam.getId().toString(),
                                        exam.getTitle(),
                                        exam.getSubject().getName(),
                                        exam.getSection().getName(),
                                        exam.getExamDate().toString(),
                                        exam.getMaximumMarks().toString(),
                                        exam.getResultStatus().toString()
                                )
                        )
                        .toList();

        // 5. Fetch recent bookings
        List<Booking> bookings = bookingRepository.findAll();
        bookings.sort(
                Comparator.comparing(
                        Booking::getBookingDate,
                        Comparator.reverseOrder()
                ).thenComparing(
                        Booking::getStartTime,
                        Comparator.reverseOrder()
                )
        );
        List<AdminDashboardActivityResponse.BookingActivityResponse>
                bookingResponses =
                bookings.stream()
                        .limit(10)
                        .map(booking ->
                                new AdminDashboardActivityResponse
                                        .BookingActivityResponse(
                                        booking.getId().toString(),
                                        booking.getFacility().getName(),
                                        booking.getBookedBy().getEmail(),
                                        booking.getBookingDate().toString(),
                                        booking.getStartTime().toString(),
                                        booking.getEndTime().toString(),
                                        booking.getPurpose(),
                                        booking.getStatus().toString()
                                )
                        )
                        .toList();

        // 6. Fetch recent notifications
        List<Notification> notifications = notificationRepository.findAll();
        notifications.sort(
                Comparator.comparing(
                        Notification::getCreatedAt,
                        Comparator.reverseOrder()
                )
        );
        List<AdminDashboardActivityResponse.NotificationActivityResponse>
                notificationResponses =
                notifications.stream()
                        .limit(10)
                        .map(notification ->
                                new AdminDashboardActivityResponse
                                        .NotificationActivityResponse(
                                        notification.getId().toString(),
                                        notification.getUser().getEmail(),
                                        notification.getTitle(),
                                        notification.getMessage(),
                                        notification.getType().toString(),
                                        notification.getCreatedAt().toString(),
                                        notification.isRead()
                                )
                        )
                        .toList();

        // Return aggregated activity response
        return new AdminDashboardActivityResponse(
                assignmentResponses,
                noticeResponses,
                examResponses,
                publishedResultResponses,
                bookingResponses,
                notificationResponses
        );
    }
}