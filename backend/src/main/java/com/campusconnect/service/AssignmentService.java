package com.campusconnect.service;

import com.campusconnect.dto.AssignmentRequest;
import com.campusconnect.dto.AssignmentResponse;
import com.campusconnect.entity.User;
import com.campusconnect.model.Assignment;
import com.campusconnect.model.Section;
import com.campusconnect.model.Student;
import com.campusconnect.model.Subject;
import com.campusconnect.model.NotificationType;
import com.campusconnect.repository.AssignmentRepository;
import com.campusconnect.repository.SectionRepository;
import com.campusconnect.repository.StudentRepository;
import com.campusconnect.repository.SubjectRepository;
import com.campusconnect.repository.TeacherAssignmentRepository;
import com.campusconnect.repository.UserRepository;

import org.springframework.core.io.Resource;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final SectionRepository sectionRepository;
    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;
    private final TeacherAssignmentRepository teacherAssignmentRepository;
    private final StudentRepository studentRepository;
    private final AssignmentFileService assignmentFileService;
    private final NotificationService notificationService;

    public AssignmentService(
            AssignmentRepository assignmentRepository,
            SectionRepository sectionRepository,
            SubjectRepository subjectRepository,
            UserRepository userRepository,
            TeacherAssignmentRepository teacherAssignmentRepository,
            StudentRepository studentRepository,
            AssignmentFileService assignmentFileService,
            NotificationService notificationService
    ) {
        this.assignmentRepository = assignmentRepository;
        this.sectionRepository = sectionRepository;
        this.subjectRepository = subjectRepository;
        this.userRepository = userRepository;
        this.teacherAssignmentRepository = teacherAssignmentRepository;
        this.studentRepository = studentRepository;
        this.assignmentFileService = assignmentFileService;
        this.notificationService = notificationService;
    }

    public AssignmentResponse createAssignment(
            AssignmentRequest request,
            String teacherEmail
    ) {

        User teacher = userRepository
                .findByEmail(teacherEmail)
                .orElseThrow(() ->
                        new RuntimeException("Teacher not found"));

        Section section = sectionRepository
                .findById(request.getSectionId())
                .orElseThrow(() ->
                        new RuntimeException("Section not found"));

        Subject subject = subjectRepository
                .findById(request.getSubjectId())
                .orElseThrow(() ->
                        new RuntimeException("Subject not found"));

        validateSubjectBelongsToSection(
                subject,
                section
        );

        validateTeacherAccess(
                teacher,
                section,
                subject
        );

        Assignment assignment = new Assignment();

        assignment.setTitle(request.getTitle());
        assignment.setDescription(request.getDescription());
        assignment.setDueDateTime(request.getDueDateTime());
        assignment.setMaximumMarks(request.getMaximumMarks());
        assignment.setSection(section);
        assignment.setSubject(subject);
        assignment.setTeacher(teacher);

        LocalDateTime now = LocalDateTime.now();

        assignment.setCreatedAt(now);
        assignment.setUpdatedAt(now);

        Assignment savedAssignment =
                assignmentRepository.save(assignment);

        // ---------------------------------------------------------
        // DAY 22: AUTOMATIC NOTIFICATION
        // ---------------------------------------------------------

        notificationService.notifyStudentsInSection(
                section.getId(),
                "New Assignment",
                "A new assignment has been posted: "
                        + savedAssignment.getTitle(),
                NotificationType.ASSIGNMENT,
                "ASSIGNMENT:" + savedAssignment.getId()
        );

        return toResponse(savedAssignment);
    }

    public List<AssignmentResponse> getAssignments(
            String userEmail
    ) {

        User user = userRepository
                .findByEmail(userEmail)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        if (user.getRole().name().equals("ADMIN")) {

            return assignmentRepository.findAll()
                    .stream()
                    .map(this::toResponse)
                    .toList();
        }

        if (user.getRole().name().equals("STUDENT")) {

            Student student = studentRepository
                    .findByUserEmail(userEmail);

            if (student == null) {
                throw new RuntimeException(
                        "Student profile not found"
                );
            }

            return assignmentRepository
                    .findBySectionId(student.getSection().getId())
                    .stream()
                    .map(this::toResponse)
                    .toList();
        }

        return assignmentRepository
                .findByTeacherId(user.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public AssignmentResponse getAssignmentById(
            UUID assignmentId,
            String userEmail
    ) {

        Assignment assignment =
                assignmentRepository.findById(assignmentId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Assignment not found"
                                ));

        validateViewAccess(
                assignment,
                userEmail
        );

        return toResponse(assignment);
    }

    public AssignmentResponse updateAssignment(
            UUID assignmentId,
            AssignmentRequest request,
            String userEmail
    ) {

        Assignment assignment =
                assignmentRepository.findById(assignmentId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Assignment not found"
                                ));

        User user = userRepository
                .findByEmail(userEmail)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        boolean isAdmin =
                user.getRole().name().equals("ADMIN");

        boolean isOwner =
                assignment.getTeacher()
                        .getId()
                        .equals(user.getId());

        if (!isAdmin && !isOwner) {
            throw new AccessDeniedException(
                    "You can only update your own assignments"
            );
        }

        Section section = sectionRepository
                .findById(request.getSectionId())
                .orElseThrow(() ->
                        new RuntimeException("Section not found"));

        Subject subject = subjectRepository
                .findById(request.getSubjectId())
                .orElseThrow(() ->
                        new RuntimeException("Subject not found"));

        validateSubjectBelongsToSection(
                subject,
                section
        );

        if (!isAdmin) {
            validateTeacherAccess(
                    user,
                    section,
                    subject
            );
        }

        assignment.setTitle(request.getTitle());
        assignment.setDescription(request.getDescription());
        assignment.setDueDateTime(request.getDueDateTime());
        assignment.setMaximumMarks(request.getMaximumMarks());
        assignment.setSection(section);
        assignment.setSubject(subject);
        assignment.setUpdatedAt(LocalDateTime.now());

        return toResponse(
                assignmentRepository.save(assignment)
        );
    }

    public void deleteAssignment(
            UUID assignmentId,
            String userEmail
    ) {

        Assignment assignment =
                assignmentRepository.findById(assignmentId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Assignment not found"
                                ));

        User user = userRepository
                .findByEmail(userEmail)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        boolean isAdmin =
                user.getRole().name().equals("ADMIN");

        boolean isOwner =
                assignment.getTeacher()
                        .getId()
                        .equals(user.getId());

        if (!isAdmin && !isOwner) {
            throw new AccessDeniedException(
                    "You can only delete your own assignments"
            );
        }

        if (assignment.getAttachmentPath() != null) {

            assignmentFileService.deleteFile(
                    assignment.getAttachmentPath()
            );
        }

        assignmentRepository.delete(assignment);
    }

    public void uploadAttachment(
            UUID assignmentId,
            MultipartFile file,
            String userEmail
    ) {

        Assignment assignment =
                assignmentRepository.findById(assignmentId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Assignment not found"
                                ));

        User user = userRepository
                .findByEmail(userEmail)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        boolean isAdmin =
                user.getRole().name().equals("ADMIN");

        boolean isOwner =
                assignment.getTeacher()
                        .getId()
                        .equals(user.getId());

        if (!isAdmin && !isOwner) {
            throw new AccessDeniedException(
                    "You can only upload attachments to your own assignments"
            );
        }

        if (!isAdmin) {
            validateTeacherAccess(
                    user,
                    assignment.getSection(),
                    assignment.getSubject()
            );
        }

        if (file == null || file.isEmpty()) {
            throw new RuntimeException(
                    "Attachment file is required"
            );
        }

        if (assignment.getAttachmentPath() != null) {
            assignmentFileService.deleteFile(
                    assignment.getAttachmentPath()
            );
        }

        String path =
                assignmentFileService.saveFile(file);

        assignment.setAttachmentPath(path);
        assignment.setUpdatedAt(LocalDateTime.now());

        assignmentRepository.save(assignment);
    }

    public Resource downloadAttachment(
            UUID assignmentId,
            String userEmail
    ) {

        Assignment assignment =
                assignmentRepository.findById(assignmentId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Assignment not found"
                                ));

        validateViewAccess(
                assignment,
                userEmail
        );

        if (assignment.getAttachmentPath() == null) {
            throw new RuntimeException(
                    "Assignment has no attachment"
            );
        }

        return assignmentFileService.loadFile(
                assignment.getAttachmentPath()
        );
    }

    private void validateViewAccess(
            Assignment assignment,
            String userEmail
    ) {

        User user = userRepository
                .findByEmail(userEmail)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        if (user.getRole().name().equals("ADMIN")) {
            return;
        }

        if (user.getRole().name().equals("TEACHER")) {

            if (!teacherAssignmentRepository
                    .existsByTeacherIdAndSectionIdAndSubjectId(
                            user.getId(),
                            assignment.getSection().getId(),
                            assignment.getSubject().getId()
                    )) {

                throw new AccessDeniedException(
                        "Teacher is not assigned to this subject and section"
                );
            }

            return;
        }

        if (user.getRole().name().equals("STUDENT")) {

            Student student =
                    studentRepository.findByUserEmail(userEmail);

            if (student == null) {
                throw new RuntimeException(
                        "Student profile not found"
                );
            }

            if (!student.getSection()
                    .getId()
                    .equals(assignment.getSection().getId())) {

                throw new AccessDeniedException(
                        "Assignment does not belong to your section"
                );
            }

            return;
        }

        throw new AccessDeniedException(
                "You are not authorized to view this assignment"
        );
    }

    private void validateTeacherAccess(
            User teacher,
            Section section,
            Subject subject
    ) {

        boolean assigned =
                teacherAssignmentRepository
                        .existsByTeacherIdAndSectionIdAndSubjectId(
                                teacher.getId(),
                                section.getId(),
                                subject.getId()
                        );

        if (!assigned) {
            throw new AccessDeniedException(
                    "Teacher is not assigned to this subject and section"
            );
        }
    }

    private void validateSubjectBelongsToSection(
            Subject subject,
            Section section
    ) {

        if (subject.getSection() == null ||
                !subject.getSection()
                        .getId()
                        .equals(section.getId())) {

            throw new IllegalArgumentException(
                    "Subject does not belong to the selected section"
            );
        }
    }

    private AssignmentResponse toResponse(
            Assignment assignment
    ) {

        return new AssignmentResponse(
                assignment.getId(),
                assignment.getTitle(),
                assignment.getDescription(),
                assignment.getDueDateTime(),
                assignment.getMaximumMarks(),

                assignment.getSection().getId(),
                assignment.getSection().getName(),

                assignment.getSubject().getId(),
                assignment.getSubject().getName(),
                assignment.getSubject().getCode(),

                assignment.getTeacher().getId(),
                assignment.getTeacher().getEmail(),

                assignment.getAttachmentPath(),

                assignment.getCreatedAt(),
                assignment.getUpdatedAt()
        );
    }
}