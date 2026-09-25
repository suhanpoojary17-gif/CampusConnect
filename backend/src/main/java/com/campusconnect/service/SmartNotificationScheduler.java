package com.campusconnect.service;

import com.campusconnect.model.Assignment;
import com.campusconnect.model.Exam;
import com.campusconnect.model.NotificationType;
import com.campusconnect.model.Student;
import com.campusconnect.model.Subject;
import com.campusconnect.entity.Attendance;
import com.campusconnect.entity.AttendanceStatus;
import com.campusconnect.repository.AssignmentRepository;
import com.campusconnect.repository.AttendanceRepository;
import com.campusconnect.repository.ExamRepository;
import com.campusconnect.repository.StudentRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.time.ZoneId;

@Service
public class SmartNotificationScheduler {

    private final AssignmentRepository assignmentRepository;
    private final ExamRepository examRepository;
    private final AttendanceRepository attendanceRepository;
    private final StudentRepository studentRepository;
    private final NotificationService notificationService;

    public SmartNotificationScheduler(
            AssignmentRepository assignmentRepository,
            ExamRepository examRepository,
            AttendanceRepository attendanceRepository,
            StudentRepository studentRepository,
            NotificationService notificationService
    ) {
        this.assignmentRepository = assignmentRepository;
        this.examRepository = examRepository;
        this.attendanceRepository = attendanceRepository;
        this.studentRepository = studentRepository;
        this.notificationService = notificationService;
    }

    /*
     * Runs every minute during development/testing.
     */
    @Scheduled(cron = "0 * * * * *")
    public void sendScheduledNotifications() {

        sendAssignmentDeadlineNotifications();

        sendExamTomorrowNotifications();

        sendLowAttendanceNotifications();
    }

    /*
     * Notify students about assignments whose
     * deadline is within the next 24 hours.
     */
    private void sendAssignmentDeadlineNotifications() {

        
        LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());

        LocalDateTime next24Hours =
                now.plusHours(24);

        List<Assignment> assignments =
                assignmentRepository.findByDueDateTimeBetween(
                        now,
                        next24Hours
                );

        for (Assignment assignment : assignments) {

            if (assignment.getSection() == null) {
                continue;
            }

            String title =
                    "Assignment Deadline Approaching";

            String message =
                    "The assignment \""
                            + assignment.getTitle()
                            + "\" is due on "
                            + assignment.getDueDateTime()
                            + ".";

            String eventKey =
                    "ASSIGNMENT_DEADLINE:"
                            + assignment.getId()
                            + ":"
                            + assignment.getDueDateTime();

            notificationService.notifyStudentsInSection(
                    assignment.getSection().getId(),
                    title,
                    message,
                    NotificationType.ASSIGNMENT,
                    eventKey
            );
        }
    }

    /*
     * Notify students about exams scheduled for tomorrow.
     */
    private void sendExamTomorrowNotifications() {

        LocalDate tomorrow =
                LocalDate.now(ZoneId.systemDefault()).plusDays(1);

        List<Exam> exams =
                examRepository.findByExamDate(tomorrow);

        for (Exam exam : exams) {

            if (exam.getSection() == null) {
                continue;
            }

            String title =
                    "Exam Tomorrow";

            String message =
                    "You have an exam tomorrow: "
                            + exam.getTitle()
                            + " at "
                            + exam.getStartTime()
                            + ".";

            String eventKey =
                    "EXAM_TOMORROW:"
                            + exam.getId()
                            + ":"
                            + exam.getExamDate();

            notificationService.notifyStudentsInSection(
                    exam.getSection().getId(),
                    title,
                    message,
                    NotificationType.EXAM,
                    eventKey
            );
        }
    }

    /*
     * Notify students whose subject attendance
     * is below 75%.
     */
        private void sendLowAttendanceNotifications() {
                List<Student> students = studentRepository.findAll();

                for (Student student : students) {
                processStudentAttendance(student);
                }
        }

        private void processStudentAttendance(Student student) {
                if (student == null || student.getUser() == null) {
                return;
                }

                List<Attendance> attendanceList = attendanceRepository.findByStudentId(student.getId());
                if (attendanceList == null || attendanceList.isEmpty()) {
                return;
                }

                Map<UUID, List<Attendance>> attendanceBySubject = groupAttendanceBySubject(attendanceList);

                for (List<Attendance> subjectAttendance : attendanceBySubject.values()) {
                checkAndNotifySubjectAttendance(student, subjectAttendance);
                }
        }

        private Map<UUID, List<Attendance>> groupAttendanceBySubject(List<Attendance> attendanceList) {
                Map<UUID, List<Attendance>> attendanceBySubject = new LinkedHashMap<>();

                for (Attendance attendance : attendanceList) {
                if (attendance != null && attendance.getSubject() != null) {
                        UUID subjectId = attendance.getSubject().getId();
                        attendanceBySubject
                                .computeIfAbsent(subjectId, key -> new java.util.ArrayList<>())
                                .add(attendance);
                }
                }

                return attendanceBySubject;
        }

        private void checkAndNotifySubjectAttendance(Student student, List<Attendance> subjectAttendance) {
                if (subjectAttendance == null || subjectAttendance.isEmpty()) {
                return;
                }

                Subject subject = subjectAttendance.get(0).getSubject();
                long totalClasses = subjectAttendance.size();

                if (totalClasses == 0) {
                return;
                }

                long presentClasses = subjectAttendance.stream()
                        .filter(attendance -> attendance.getStatus() == AttendanceStatus.PRESENT)
                        .count();

                double attendancePercentage = (presentClasses * 100.0) / totalClasses;

                if (attendancePercentage < 75.0) {
                sendLowAttendanceNotification(student, subject, attendancePercentage);
                }
        }

        private void sendLowAttendanceNotification(Student student, Subject subject, double attendancePercentage) {
                double roundedPercentage = Math.round(attendancePercentage * 100.0) / 100.0;

                String title = "Low Attendance Alert";
                String message = "Your attendance in "
                        + subject.getName()
                        + " is "
                        + roundedPercentage
                        + "%. "
                        + "Your attendance is below "
                        + "the required 75%.";

                // Explicit ZoneId specified to resolve java:S8688
                String eventKey = "LOW_ATTENDANCE:"
                        + student.getId()
                        + ":"
                        + subject.getId()
                        + ":"
                        + LocalDate.now(ZoneId.systemDefault());

                notificationService.createNotification(
                        student.getUser().getId(),
                        title,
                        message,
                        NotificationType.ATTENDANCE,
                        eventKey
                );
        }
    
}