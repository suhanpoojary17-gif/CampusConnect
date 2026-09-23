package com.campusconnect.service;

import com.campusconnect.dto.StudentMarkRequest;
import com.campusconnect.dto.StudentMarkResponse;
import com.campusconnect.entity.User;
import com.campusconnect.model.Assessment;
import com.campusconnect.model.Student;
import com.campusconnect.model.StudentMark;
import com.campusconnect.repository.AssessmentRepository;
import com.campusconnect.repository.StudentMarkRepository;
import com.campusconnect.repository.StudentRepository;
import com.campusconnect.repository.TeacherAssignmentRepository;
import com.campusconnect.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class StudentMarkService {

    private final StudentMarkRepository studentMarkRepository;
    private final AssessmentRepository assessmentRepository;
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final TeacherAssignmentRepository teacherAssignmentRepository;

    public StudentMarkService(
            StudentMarkRepository studentMarkRepository,
            AssessmentRepository assessmentRepository,
            StudentRepository studentRepository,
            UserRepository userRepository,
            TeacherAssignmentRepository teacherAssignmentRepository
    ) {
        this.studentMarkRepository = studentMarkRepository;
        this.assessmentRepository = assessmentRepository;
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
        this.teacherAssignmentRepository = teacherAssignmentRepository;
    }

    @Transactional
    public StudentMarkResponse saveMark(
            UUID assessmentId,
            StudentMarkRequest request,
            String userEmail
    ) {

        User user = getUserByEmail(userEmail);

        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() ->
                        new RuntimeException("Assessment not found")
                );

        validateAssessmentAccess(user, assessment);

        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() ->
                        new RuntimeException("Student not found")
                );

        validateStudentSection(student, assessment);

        validateMarks(
                request.getMarks(),
                assessment.getMaximumMarks()
        );

        StudentMark studentMark =
                studentMarkRepository
                        .findByAssessmentIdAndStudentId(
                                assessmentId,
                                student.getId()
                        )
                        .orElseGet(StudentMark::new);

        studentMark.setAssessment(assessment);
        studentMark.setStudent(student);
        studentMark.setMarks(request.getMarks());
        studentMark.setGradedAt(LocalDateTime.now());

        StudentMark saved =
                studentMarkRepository.save(studentMark);

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<StudentMarkResponse> getMarksForAssessment(
            UUID assessmentId,
            String userEmail
    ) {

        User user = getUserByEmail(userEmail);

        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() ->
                        new RuntimeException("Assessment not found")
                );

        validateAssessmentAccess(user, assessment);

        return studentMarkRepository
                .findByAssessmentId(assessmentId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<StudentMarkResponse> getMyMarks(
            String userEmail
    ) {

        User user = getUserByEmail(userEmail);

        if (!user.getRole().name().equals("STUDENT")) {
            throw new RuntimeException(
                    "Only students can access their marks"
            );
        }

        Student student = studentRepository.findByUserEmail(userEmail);

        if (student == null) {
            throw new RuntimeException("Student profile not found");
        }

        return studentMarkRepository
                .findByStudentId(student.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private void validateAssessmentAccess(
            User user,
            Assessment assessment
    ) {

        String role = user.getRole().name();

        if (role.equals("ADMIN")) {
            return;
        }

        if (role.equals("TEACHER")) {

            if (!assessment.getTeacher().getId().equals(user.getId())) {
                throw new RuntimeException(
                        "Teacher can only manage their own assessments"
                );
            }

            boolean assigned =
                    teacherAssignmentRepository
                            .existsByTeacherIdAndSectionIdAndSubjectId(
                                    user.getId(),
                                    assessment.getSection().getId(),
                                    assessment.getSubject().getId()
                            );

            if (!assigned) {
                throw new RuntimeException(
                        "Teacher is not assigned to this section and subject"
                );
            }

            return;
        }

        throw new RuntimeException(
                "Students cannot manage assessment marks"
        );
    }

    private void validateStudentSection(
            Student student,
            Assessment assessment
    ) {

        if (student.getSection() == null
                || !student.getSection().getId()
                .equals(assessment.getSection().getId())) {

            throw new RuntimeException(
                    "Student does not belong to the assessment section"
            );
        }
    }

    private void validateMarks(
            BigDecimal marks,
            BigDecimal maximumMarks
    ) {

        if (marks.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException(
                    "Marks cannot be negative"
            );
        }

        if (marks.compareTo(maximumMarks) > 0) {
            throw new RuntimeException(
                    "Marks cannot exceed maximum marks"
            );
        }
    }

    private User getUserByEmail(String email) {

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found")
                );
    }

    private StudentMarkResponse mapToResponse(
            StudentMark mark
    ) {

        StudentMarkResponse response =
                new StudentMarkResponse();

        response.setId(mark.getId());

        response.setAssessmentId(
                mark.getAssessment().getId()
        );

        response.setAssessmentTitle(
                mark.getAssessment().getTitle()
        );

        response.setMaximumMarks(
                mark.getAssessment().getMaximumMarks()
        );

        response.setStudentId(
                mark.getStudent().getId()
        );

        response.setStudentName(
                mark.getStudent().getName()
        );

        response.setMarks(mark.getMarks());

        response.setGradedAt(mark.getGradedAt());

        return response;
    }
}