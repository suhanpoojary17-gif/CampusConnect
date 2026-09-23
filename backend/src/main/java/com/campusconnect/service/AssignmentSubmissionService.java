package com.campusconnect.service;

import com.campusconnect.dto.AssignmentSubmissionRequest;
import com.campusconnect.dto.AssignmentSubmissionResponse;
import com.campusconnect.entity.User;
import com.campusconnect.model.Assignment;
import com.campusconnect.model.AssignmentSubmission;
import com.campusconnect.model.Student;
import com.campusconnect.repository.AssignmentRepository;
import com.campusconnect.repository.AssignmentSubmissionRepository;
import com.campusconnect.repository.StudentRepository;
import com.campusconnect.repository.TeacherAssignmentRepository;
import com.campusconnect.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class AssignmentSubmissionService {

    private final AssignmentSubmissionRepository submissionRepository;
    private final AssignmentRepository assignmentRepository;
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final TeacherAssignmentRepository teacherAssignmentRepository;
    private final AssignmentSubmissionFileService fileService;

    public AssignmentSubmissionService(
            AssignmentSubmissionRepository submissionRepository,
            AssignmentRepository assignmentRepository,
            StudentRepository studentRepository,
            UserRepository userRepository,
            TeacherAssignmentRepository teacherAssignmentRepository,
            AssignmentSubmissionFileService fileService
    ) {
        this.submissionRepository = submissionRepository;
        this.assignmentRepository = assignmentRepository;
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
        this.teacherAssignmentRepository = teacherAssignmentRepository;
        this.fileService = fileService;
    }

    // =========================
    // STUDENT SUBMIT / RESUBMIT
    // =========================

    public AssignmentSubmissionResponse submitAssignment(
            UUID assignmentId,
            AssignmentSubmissionRequest request,
            MultipartFile file,
            String studentEmail
    ) {

        Student student =
                studentRepository.findByUserEmail(studentEmail);

        if (student == null) {
            throw new RuntimeException(
                    "Student profile not found"
            );
        }

        Assignment assignment =
                assignmentRepository.findById(assignmentId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Assignment not found"
                                )
                        );

        // Student must belong to the assignment's section
        if (!student.getSection()
                .getId()
                .equals(assignment.getSection().getId())) {

            throw new RuntimeException(
                    "You can only submit assignments for your section"
            );
        }

        LocalDateTime now = LocalDateTime.now();

        // Submission after deadline is not allowed
        if (now.isAfter(assignment.getDueDateTime())) {

            throw new RuntimeException(
                    "Assignment submission deadline has passed"
            );
        }

        if (file == null || file.isEmpty()) {
            throw new RuntimeException(
                    "Submission file is required"
            );
        }

        AssignmentSubmission submission =
                submissionRepository
                        .findByAssignmentIdAndStudentId(
                                assignmentId,
                                student.getId()
                        )
                        .orElse(null);

        // =========================
        // RESUBMISSION
        // =========================

        if (submission != null) {

            // Delete previous file
            fileService.deleteFile(
                    submission.getAttachmentPath()
            );

        } else {

            // =========================
            // FIRST SUBMISSION
            // =========================

            submission = new AssignmentSubmission();

            submission.setAssignment(assignment);
            submission.setStudent(student);
        }

        String attachmentPath =
                fileService.saveFile(file);

        submission.setAttachmentPath(attachmentPath);
        submission.setSubmittedAt(now);

        // Since submission after deadline is rejected,
        // accepted submissions are currently not late.
        submission.setSubmittedLate(false);

        if (request != null) {
            submission.setComment(request.getComment());
        } else {
            submission.setComment(null);
        }

        AssignmentSubmission saved =
                submissionRepository.save(submission);

        return mapToResponse(saved);
    }

    // =========================
    // STUDENT - OWN SUBMISSION
    // =========================

    public AssignmentSubmissionResponse getMySubmission(
            UUID assignmentId,
            String studentEmail
    ) {

        Student student =
                studentRepository.findByUserEmail(studentEmail);

        if (student == null) {
            throw new RuntimeException(
                    "Student profile not found"
            );
        }

        Assignment assignment =
                assignmentRepository.findById(assignmentId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Assignment not found"
                                )
                        );

        if (!student.getSection()
                .getId()
                .equals(assignment.getSection().getId())) {

            throw new RuntimeException(
                    "You can only access submissions for your section"
            );
        }

        AssignmentSubmission submission =
                submissionRepository
                        .findByAssignmentIdAndStudentId(
                                assignmentId,
                                student.getId()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "You have not submitted this assignment"
                                )
                        );

        return mapToResponse(submission);
    }

    // =========================
    // TEACHER / ADMIN
    // GET ALL SUBMISSIONS
    // =========================

    public List<AssignmentSubmissionResponse> getSubmissions(
            UUID assignmentId,
            String userEmail
    ) {

        User user =
                userRepository.findByEmail(userEmail)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "User not found"
                                )
                        );

        Assignment assignment =
                assignmentRepository.findById(assignmentId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Assignment not found"
                                )
                        );

        // Admin can view everything
        if (user.getRole().name().equals("ADMIN")) {

            return submissionRepository
                    .findByAssignmentId(assignmentId)
                    .stream()
                    .map(this::mapToResponse)
                    .toList();
        }

        // Teacher authorization
        if (user.getRole().name().equals("TEACHER")) {

            boolean assigned =
                    teacherAssignmentRepository
                            .existsByTeacherIdAndSectionIdAndSubjectId(
                                    user.getId(),
                                    assignment.getSection().getId(),
                                    assignment.getSubject().getId()
                            );

            if (!assigned) {
                throw new RuntimeException(
                        "Teacher is not assigned to this assignment"
                );
            }

            return submissionRepository
                    .findByAssignmentId(assignmentId)
                    .stream()
                    .map(this::mapToResponse)
                    .toList();
        }

        throw new RuntimeException(
                "You are not authorized to view all submissions"
        );
    }

    // =========================
    // GET SINGLE SUBMISSION
    // =========================

    public AssignmentSubmissionResponse getSubmission(
            UUID submissionId,
            String userEmail
    ) {

        AssignmentSubmission submission =
                submissionRepository.findById(submissionId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Submission not found"
                                )
                        );

        User user =
                userRepository.findByEmail(userEmail)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "User not found"
                                )
                        );

        // Admin
        if (user.getRole().name().equals("ADMIN")) {
            return mapToResponse(submission);
        }

        // Student can only access own submission
        if (user.getRole().name().equals("STUDENT")) {

            Student student =
                    studentRepository.findByUserEmail(userEmail);

            if (student == null ||
                    !submission.getStudent()
                            .getId()
                            .equals(student.getId())) {

                throw new RuntimeException(
                        "You can only access your own submission"
                );
            }

            return mapToResponse(submission);
        }

        // Teacher must be assigned to the assignment
        if (user.getRole().name().equals("TEACHER")) {

            Assignment assignment =
                    submission.getAssignment();

            boolean assigned =
                    teacherAssignmentRepository
                            .existsByTeacherIdAndSectionIdAndSubjectId(
                                    user.getId(),
                                    assignment.getSection().getId(),
                                    assignment.getSubject().getId()
                            );

            if (!assigned) {
                throw new RuntimeException(
                        "Teacher is not assigned to this assignment"
                );
            }

            return mapToResponse(submission);
        }

        throw new RuntimeException(
                "You are not authorized to access this submission"
        );
    }

    // =========================
    // FILE DOWNLOAD AUTH CHECK
    // =========================

    public AssignmentSubmission getSubmissionEntityForDownload(
            UUID submissionId,
            String userEmail
    ) {

        AssignmentSubmission submission =
                submissionRepository.findById(submissionId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Submission not found"
                                )
                        );

        User user =
                userRepository.findByEmail(userEmail)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "User not found"
                                )
                        );

        // Admin
        if (user.getRole().name().equals("ADMIN")) {
            return submission;
        }

        // Student
        if (user.getRole().name().equals("STUDENT")) {

            Student student =
                    studentRepository.findByUserEmail(userEmail);

            if (student == null ||
                    !submission.getStudent()
                            .getId()
                            .equals(student.getId())) {

                throw new RuntimeException(
                        "You can only download your own submission"
                );
            }

            return submission;
        }

        // Teacher
        if (user.getRole().name().equals("TEACHER")) {

            Assignment assignment =
                    submission.getAssignment();

            boolean assigned =
                    teacherAssignmentRepository
                            .existsByTeacherIdAndSectionIdAndSubjectId(
                                    user.getId(),
                                    assignment.getSection().getId(),
                                    assignment.getSubject().getId()
                            );

            if (!assigned) {
                throw new RuntimeException(
                        "Teacher is not assigned to this assignment"
                );
            }

            return submission;
        }

        throw new RuntimeException(
                "You are not authorized to download this submission"
        );
    }

    // =========================
    // RESPONSE MAPPER
    // =========================

    private AssignmentSubmissionResponse mapToResponse(
            AssignmentSubmission submission
    ) {

        Assignment assignment =
                submission.getAssignment();

        Student student =
                submission.getStudent();

        AssignmentSubmissionResponse response =
                new AssignmentSubmissionResponse();

        response.setId(submission.getId());

        response.setAssignmentId(
                assignment.getId()
        );

        response.setAssignmentTitle(
                assignment.getTitle()
        );

        response.setDueDateTime(
                assignment.getDueDateTime()
        );

        response.setMaximumMarks(
                assignment.getMaximumMarks()
        );

        response.setStudentId(
                student.getId()
        );

        response.setStudentName(
                student.getName()
        );

        if (student.getUser() != null) {
            response.setStudentEmail(
                    student.getUser().getEmail()
            );
        }

        response.setSubmittedAt(
                submission.getSubmittedAt()
        );

        response.setSubmittedLate(
                submission.getSubmittedLate()
        );

        response.setComment(
                submission.getComment()
        );

        response.setAttachmentPath(
                submission.getAttachmentPath()
        );

        return response;
    }
}