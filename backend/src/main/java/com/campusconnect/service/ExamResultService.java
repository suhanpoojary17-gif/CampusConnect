package com.campusconnect.service;

import com.campusconnect.entity.Role;
import com.campusconnect.entity.User;
import com.campusconnect.model.Exam;
import com.campusconnect.model.ExamResultStatus;
import com.campusconnect.repository.ExamRepository;
import com.campusconnect.repository.TeacherAssignmentRepository;
import com.campusconnect.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ExamResultService {

    private final ExamRepository examRepository;
    private final UserRepository userRepository;
    private final TeacherAssignmentRepository teacherAssignmentRepository;
    private final NotificationService notificationService;

    public ExamResultService(
            ExamRepository examRepository,
            UserRepository userRepository,
            TeacherAssignmentRepository teacherAssignmentRepository,
            NotificationService notificationService
    ) {
        this.examRepository = examRepository;
        this.userRepository = userRepository;
        this.teacherAssignmentRepository = teacherAssignmentRepository;
        this.notificationService = notificationService;
    }
    public String submitResult(
            UUID examId,
            String email
    ) {

        User teacher = getTeacher(email);

        Exam exam = examRepository.findById(examId)
                .orElseThrow(() ->
                        new RuntimeException("Exam not found")
                );

        validateTeacherAssignment(teacher, exam);

        if (!exam.getExamDate().isBefore(
                java.time.LocalDate.now()
        )) {
            throw new RuntimeException(
                    "Exam result can only be submitted after the exam date"
            );
        }

        if (exam.getResultStatus() ==
                ExamResultStatus.PUBLISHED) {

            throw new RuntimeException(
                    "Published result cannot be submitted again"
            );
        }

    exam.setResultStatus(
            ExamResultStatus.PUBLISHED
    );

    examRepository.save(exam);

    notificationService.notifyStudentsInSection(
            exam.getSection().getId(),
            "Result Published",
            "The result for "
                    + exam.getTitle()
                    + " has been published.",
            com.campusconnect.model.NotificationType.RESULT
    );

    return "Exam result published successfully";
    }

    public String publishResult(
            UUID examId,
            String email
    ) {

        User admin = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found")
                );

        if (admin.getRole() != Role.ADMIN) {
            throw new RuntimeException(
                    "Only admin can publish results"
            );
        }

        Exam exam = examRepository.findById(examId)
                .orElseThrow(() ->
                        new RuntimeException("Exam not found")
                );

        if (exam.getResultStatus() !=
                ExamResultStatus.SUBMITTED) {

            throw new RuntimeException(
                    "Only submitted results can be published"
            );
        }

        exam.setResultStatus(
                ExamResultStatus.PUBLISHED
        );

        examRepository.save(exam);

        return "Exam result published successfully";
    }

    private User getTeacher(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found")
                );

        if (user.getRole() != Role.TEACHER) {
            throw new RuntimeException(
                    "Only teachers can perform this action"
            );
        }

        return user;
    }

    private void validateTeacherAssignment(
            User teacher,
            Exam exam
    ) {

        boolean assigned =
                teacherAssignmentRepository
                        .existsByTeacherIdAndSectionIdAndSubjectId(
                                teacher.getId(),
                                exam.getSection().getId(),
                                exam.getSubject().getId()
                        );

        if (!assigned) {
            throw new RuntimeException(
                    "You are not assigned to this subject for this section"
            );
        }
    }
}