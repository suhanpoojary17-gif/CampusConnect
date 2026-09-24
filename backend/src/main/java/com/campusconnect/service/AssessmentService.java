package com.campusconnect.service;

import com.campusconnect.dto.AssessmentRequest;
import com.campusconnect.dto.AssessmentResponse;
import com.campusconnect.entity.User;
import com.campusconnect.model.Assessment;
import com.campusconnect.model.Section;
import com.campusconnect.model.Subject;
import com.campusconnect.repository.AssessmentRepository;
import com.campusconnect.repository.SectionRepository;
import com.campusconnect.repository.SubjectRepository;
import com.campusconnect.repository.TeacherAssignmentRepository;
import com.campusconnect.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.UUID;

@Service
public class AssessmentService {

    private final AssessmentRepository assessmentRepository;
    private final SectionRepository sectionRepository;
    private final SubjectRepository subjectRepository;
    private final TeacherAssignmentRepository teacherAssignmentRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public AssessmentService(
                AssessmentRepository assessmentRepository,
                SectionRepository sectionRepository,
                SubjectRepository subjectRepository,
                TeacherAssignmentRepository teacherAssignmentRepository,
                UserRepository userRepository,
                NotificationService notificationService
        ) {
        this.assessmentRepository = assessmentRepository;
        this.sectionRepository = sectionRepository;
        this.subjectRepository = subjectRepository;
        this.teacherAssignmentRepository = teacherAssignmentRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        }

    @Transactional
    public AssessmentResponse createAssessment(
            AssessmentRequest request,
            String teacherEmail
    ) {

        User teacher = getUserByEmail(teacherEmail);

        Section section = sectionRepository.findById(request.getSectionId())
                .orElseThrow(() ->
                        new RuntimeException("Section not found")
                );

        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() ->
                        new RuntimeException("Subject not found")
                );

        validateSubjectBelongsToSection(subject, request.getSectionId());

        validateTeacherAssignment(
                teacher.getId(),
                request.getSectionId(),
                request.getSubjectId()
        );

        Assessment assessment = new Assessment();

        assessment.setTitle(request.getTitle());
        assessment.setDescription(request.getDescription());
        assessment.setType(request.getType());
        assessment.setMaximumMarks(request.getMaximumMarks());
        assessment.setAssessmentDate(request.getAssessmentDate());
        assessment.setSection(section);
        assessment.setSubject(subject);
        assessment.setTeacher(teacher);

        Assessment saved = assessmentRepository.save(assessment);

        notificationService.notifyStudentsInSection(
                section.getId(),
                "New Assessment",
                "A new assessment has been created: "
                        + saved.getTitle()
                        + " (" + subject.getName() + ")",
                com.campusconnect.model.NotificationType.ASSESSMENT
        );

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<AssessmentResponse> getAllAssessments(String userEmail) {

        User user = getUserByEmail(userEmail);

        if (user.getRole().name().equals("ADMIN")) {

            return assessmentRepository.findAll()
                    .stream()
                    .map(this::mapToResponse)
                    .toList();
        }

        if (user.getRole().name().equals("TEACHER")) {

            return assessmentRepository.findByTeacherId(user.getId())
                    .stream()
                    .map(this::mapToResponse)
                    .toList();
        }

        throw new RuntimeException(
                "Students should use the student assessment endpoint"
        );
    }

    @Transactional(readOnly = true)
    public AssessmentResponse getAssessmentById(UUID id) {

        Assessment assessment = assessmentRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Assessment not found")
                );

        return mapToResponse(assessment);
    }

    @Transactional
    public AssessmentResponse updateAssessment(
            UUID id,
            AssessmentRequest request,
            String teacherEmail
    ) {

        User user = getUserByEmail(teacherEmail);

        Assessment assessment = assessmentRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Assessment not found")
                );

        if (user.getRole().name().equals("TEACHER")
                && !assessment.getTeacher().getId().equals(user.getId())) {

            throw new RuntimeException(
                    "Teacher can only update their own assessments"
            );
        }

        Section section = sectionRepository.findById(request.getSectionId())
                .orElseThrow(() ->
                        new RuntimeException("Section not found")
                );

        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() ->
                        new RuntimeException("Subject not found")
                );

        validateSubjectBelongsToSection(subject, request.getSectionId());

        if (user.getRole().name().equals("TEACHER")) {

            validateTeacherAssignment(
                    user.getId(),
                    request.getSectionId(),
                    request.getSubjectId()
            );
        }

        assessment.setTitle(request.getTitle());
        assessment.setDescription(request.getDescription());
        assessment.setType(request.getType());
        assessment.setMaximumMarks(request.getMaximumMarks());
        assessment.setAssessmentDate(request.getAssessmentDate());
        assessment.setSection(section);
        assessment.setSubject(subject);

        Assessment updated = assessmentRepository.save(assessment);

        return mapToResponse(updated);
    }

    @Transactional
    public void deleteAssessment(
            UUID id,
            String userEmail
    ) {

        User user = getUserByEmail(userEmail);

        Assessment assessment = assessmentRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Assessment not found")
                );

        if (user.getRole().name().equals("TEACHER")
                && !assessment.getTeacher().getId().equals(user.getId())) {

            throw new RuntimeException(
                    "Teacher can only delete their own assessments"
            );
        }

        assessmentRepository.delete(assessment);
    }

    private User getUserByEmail(String email) {

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found")
                );
    }

    private void validateSubjectBelongsToSection(
            Subject subject,
            UUID sectionId
    ) {

        if (subject.getSection() == null
                || !subject.getSection().getId().equals(sectionId)) {

            throw new RuntimeException(
                    "Subject does not belong to the selected section"
            );
        }
    }

    private void validateTeacherAssignment(
            Long teacherId,
            UUID sectionId,
            UUID subjectId
    ) {

        boolean assigned =
                teacherAssignmentRepository
                        .existsByTeacherIdAndSectionIdAndSubjectId(
                                teacherId,
                                sectionId,
                                subjectId
                        );

        if (!assigned) {

            throw new RuntimeException(
                    "Teacher is not assigned to this section and subject"
            );
        }
    }

    private AssessmentResponse mapToResponse(
            Assessment assessment
    ) {

        AssessmentResponse response = new AssessmentResponse();

        response.setId(assessment.getId());
        response.setTitle(assessment.getTitle());
        response.setDescription(assessment.getDescription());
        response.setType(assessment.getType());
        response.setMaximumMarks(assessment.getMaximumMarks());
        response.setAssessmentDate(assessment.getAssessmentDate());

        response.setSectionId(
                assessment.getSection().getId()
        );

        response.setSectionName(
                assessment.getSection().getName()
        );

        response.setSubjectId(
                assessment.getSubject().getId()
        );

        response.setSubjectName(
                assessment.getSubject().getName()
        );

        response.setSubjectCode(
                assessment.getSubject().getCode()
        );

        response.setTeacherId(
                assessment.getTeacher().getId()
        );

        response.setTeacherEmail(
                assessment.getTeacher().getEmail()
        );

        response.setCreatedAt(
                assessment.getCreatedAt()
        );

        response.setUpdatedAt(
                assessment.getUpdatedAt()
        );

        return response;
    }
}