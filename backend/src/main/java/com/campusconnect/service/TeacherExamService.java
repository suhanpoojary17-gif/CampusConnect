package com.campusconnect.service;

import com.campusconnect.dto.ExamMarkEntryRequest;
import com.campusconnect.dto.ExamMarkResponse;
import com.campusconnect.dto.ExamResponse;
import com.campusconnect.entity.Role;
import com.campusconnect.entity.TeacherAssignment;
import com.campusconnect.entity.User;
import com.campusconnect.model.Exam;
import com.campusconnect.model.ExamMark;
import com.campusconnect.model.Student;
import com.campusconnect.repository.ExamMarkRepository;
import com.campusconnect.repository.ExamRepository;
import com.campusconnect.repository.StudentRepository;
import com.campusconnect.repository.TeacherAssignmentRepository;
import com.campusconnect.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class TeacherExamService {

    private final ExamRepository examRepository;
    private final ExamMarkRepository examMarkRepository;
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final TeacherAssignmentRepository teacherAssignmentRepository;

    public TeacherExamService(
            ExamRepository examRepository,
            ExamMarkRepository examMarkRepository,
            StudentRepository studentRepository,
            UserRepository userRepository,
            TeacherAssignmentRepository teacherAssignmentRepository
    ) {
        this.examRepository = examRepository;
        this.examMarkRepository = examMarkRepository;
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
        this.teacherAssignmentRepository = teacherAssignmentRepository;
    }

    public List<ExamResponse> getMyEligibleExams(String email) {

        User teacher = getTeacher(email);

        return examRepository.findAll()
                .stream()
                .filter(exam ->
                        exam.getExamDate().isBefore(LocalDate.now())
                )
                .filter(exam ->
                        teacherAssignmentRepository
                                .existsByTeacherIdAndSectionIdAndSubjectId(
                                        teacher.getId(),
                                        exam.getSection().getId(),
                                        exam.getSubject().getId()
                                )
                )
                .map(this::toExamResponse)
                .toList();
    }

    public List<ExamMarkResponse> enterBulkMarks(
            UUID examId,
            List<ExamMarkEntryRequest> entries,
            String email
    ) {

        User teacher = getTeacher(email);

        Exam exam = examRepository.findById(examId)
                .orElseThrow(() ->
                        new RuntimeException("Exam not found")
                );

        validateExamDatePassed(exam);

        validateTeacherAssignment(teacher, exam);

        List<Student> sectionStudents =
                studentRepository.findBySectionId(
                        exam.getSection().getId()
                );

        List<UUID> validStudentIds = sectionStudents
                .stream()
                .map(Student::getId)
                .toList();

        for (ExamMarkEntryRequest entry : entries) {

            if (!validStudentIds.contains(entry.getStudentId())) {
                throw new RuntimeException(
                        "Student does not belong to the exam section"
                );
            }

            if (entry.getMarks().compareTo(BigDecimal.ZERO) < 0) {
                throw new RuntimeException(
                        "Marks cannot be negative"
                );
            }

            if (entry.getMarks().compareTo(exam.getMaximumMarks()) > 0) {
                throw new RuntimeException(
                        "Marks cannot exceed maximum marks of "
                                + exam.getMaximumMarks()
                );
            }
        }

        for (ExamMarkEntryRequest entry : entries) {

            Student student = sectionStudents
                    .stream()
                    .filter(s ->
                            s.getId().equals(entry.getStudentId())
                    )
                    .findFirst()
                    .orElseThrow(() ->
                            new RuntimeException("Student not found")
                    );

            ExamMark examMark =
                    examMarkRepository
                            .findByExamIdAndStudentId(
                                    examId,
                                    student.getId()
                            )
                            .orElseGet(() -> {

                                ExamMark newMark = new ExamMark();

                                newMark.setExam(exam);
                                newMark.setStudent(student);

                                return newMark;
                            });

            examMark.setMarks(entry.getMarks());
            examMark.setGradedAt(
                    java.time.LocalDateTime.now()
            );

            examMarkRepository.save(examMark);
        }

        return getExamMarks(examId, email);
    }

    public List<ExamMarkResponse> getExamMarks(
            UUID examId,
            String email
    ) {

        User teacher = getTeacher(email);

        Exam exam = examRepository.findById(examId)
                .orElseThrow(() ->
                        new RuntimeException("Exam not found")
                );

        validateTeacherAssignment(teacher, exam);

        return examMarkRepository.findByExamId(examId)
                .stream()
                .map(this::toMarkResponse)
                .toList();
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

    private void validateExamDatePassed(Exam exam) {

        if (!exam.getExamDate().isBefore(LocalDate.now())) {
            throw new RuntimeException(
                    "Exam marks can only be entered after the exam date"
            );
        }
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

    private ExamResponse toExamResponse(Exam exam) {

        ExamResponse response = new ExamResponse();

        response.setId(exam.getId());
        response.setTitle(exam.getTitle());
        response.setDescription(exam.getDescription());
        response.setExamDate(exam.getExamDate());
        response.setStartTime(exam.getStartTime());
        response.setEndTime(exam.getEndTime());
        response.setMaximumMarks(exam.getMaximumMarks());

        response.setSectionId(
                exam.getSection().getId()
        );

        response.setSectionName(
                exam.getSection().getName()
        );

        response.setSubjectId(
                exam.getSubject().getId()
        );

        response.setSubjectName(
                exam.getSubject().getName()
        );

        response.setSubjectCode(
                exam.getSubject().getCode()
        );

        return response;
    }

    private ExamMarkResponse toMarkResponse(
            ExamMark examMark
    ) {

        ExamMarkResponse response =
                new ExamMarkResponse();

        Exam exam = examMark.getExam();
        Student student = examMark.getStudent();

        response.setId(examMark.getId());
        response.setExamId(exam.getId());
        response.setExamTitle(exam.getTitle());
        response.setMaximumMarks(exam.getMaximumMarks());

        response.setStudentId(student.getId());
        response.setStudentName(student.getName());

        response.setMarks(examMark.getMarks());

        BigDecimal percentage =
                examMark.getMarks()
                        .multiply(BigDecimal.valueOf(100))
                        .divide(
                                exam.getMaximumMarks(),
                                2,
                                RoundingMode.HALF_UP
                        );

        response.setPercentage(percentage);
        response.setGradedAt(examMark.getGradedAt());

        return response;
    }
}