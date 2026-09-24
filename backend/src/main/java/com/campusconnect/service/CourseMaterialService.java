package com.campusconnect.service;

import com.campusconnect.dto.CourseMaterialRequest;
import com.campusconnect.dto.CourseMaterialResponse;
import com.campusconnect.entity.User;
import com.campusconnect.model.CourseMaterial;
import com.campusconnect.model.Section;
import com.campusconnect.model.Student;
import com.campusconnect.model.Subject;
import com.campusconnect.model.NotificationType;
import com.campusconnect.repository.CourseMaterialRepository;
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
public class CourseMaterialService {

    private final CourseMaterialRepository courseMaterialRepository;
    private final SectionRepository sectionRepository;
    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;
    private final TeacherAssignmentRepository teacherAssignmentRepository;
    private final StudentRepository studentRepository;
    private final CourseMaterialFileService courseMaterialFileService;
    private final NotificationService notificationService;

    public CourseMaterialService(
            CourseMaterialRepository courseMaterialRepository,
            SectionRepository sectionRepository,
            SubjectRepository subjectRepository,
            UserRepository userRepository,
            TeacherAssignmentRepository teacherAssignmentRepository,
            StudentRepository studentRepository,
            CourseMaterialFileService courseMaterialFileService,
            NotificationService notificationService
    ) {
        this.courseMaterialRepository = courseMaterialRepository;
        this.sectionRepository = sectionRepository;
        this.subjectRepository = subjectRepository;
        this.userRepository = userRepository;
        this.teacherAssignmentRepository =
                teacherAssignmentRepository;
        this.studentRepository = studentRepository;
        this.courseMaterialFileService =
                courseMaterialFileService;
        this.notificationService = notificationService;
    }

    // ---------------------------------------------------------
    // CREATE
    // ---------------------------------------------------------

    public CourseMaterialResponse createMaterial(
            CourseMaterialRequest request,
            String teacherEmail
    ) {

        User teacher =
                userRepository.findByEmail(teacherEmail)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Teacher not found"
                                ));

        Section section =
                sectionRepository.findById(
                        request.getSectionId()
                ).orElseThrow(() ->
                        new RuntimeException(
                                "Section not found"
                        ));

        Subject subject =
                subjectRepository.findById(
                        request.getSubjectId()
                ).orElseThrow(() ->
                        new RuntimeException(
                                "Subject not found"
                        ));

        validateSubjectBelongsToSection(
                subject,
                section
        );

        validateTeacherAccess(
                teacher,
                section,
                subject
        );

        CourseMaterial material =
                new CourseMaterial();

        material.setTitle(
                request.getTitle()
        );

        material.setDescription(
                request.getDescription()
        );

        material.setSection(section);
        material.setSubject(subject);
        material.setTeacher(teacher);

        LocalDateTime now =
                LocalDateTime.now();

        material.setCreatedAt(now);
        material.setUpdatedAt(now);

        CourseMaterial savedMaterial =
                courseMaterialRepository.save(material);

        // ---------------------------------------------------------
        // DAY 22: AUTOMATIC NOTIFICATION
        // ---------------------------------------------------------

        notificationService.notifyStudentsInSection(
                section.getId(),
                "New Course Material",
                "New study material has been uploaded: "
                        + savedMaterial.getTitle(),
                NotificationType.COURSE_MATERIAL,
                "COURSE_MATERIAL:" + savedMaterial.getId()
        );

        return toResponse(savedMaterial);
    }

    // ---------------------------------------------------------
    // GET ALL
    // ---------------------------------------------------------

    public List<CourseMaterialResponse> getMaterials(
            String userEmail
    ) {

        User user =
                userRepository.findByEmail(userEmail)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "User not found"
                                ));

        if (user.getRole().name().equals("ADMIN")) {

            return courseMaterialRepository
                    .findAll()
                    .stream()
                    .map(this::toResponse)
                    .toList();
        }

        if (user.getRole().name().equals("STUDENT")) {

            Student student =
                    studentRepository.findByUserEmail(
                            userEmail
                    );

            if (student == null) {

                throw new RuntimeException(
                        "Student profile not found"
                );
            }

            return courseMaterialRepository
                    .findBySectionId(
                            student.getSection().getId()
                    )
                    .stream()
                    .map(this::toResponse)
                    .toList();
        }

        return courseMaterialRepository
                .findByTeacherId(user.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // ---------------------------------------------------------
    // GET BY ID
    // ---------------------------------------------------------

    public CourseMaterialResponse getMaterialById(
            UUID materialId,
            String userEmail
    ) {

        CourseMaterial material =
                courseMaterialRepository.findById(materialId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Course material not found"
                                ));

        validateViewAccess(
                material,
                userEmail
        );

        return toResponse(material);
    }

    // ---------------------------------------------------------
    // UPDATE
    // ---------------------------------------------------------

    public CourseMaterialResponse updateMaterial(
            UUID materialId,
            CourseMaterialRequest request,
            String userEmail
    ) {

        CourseMaterial material =
                courseMaterialRepository.findById(materialId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Course material not found"
                                ));

        User user =
                userRepository.findByEmail(userEmail)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "User not found"
                                ));

        boolean isAdmin =
                user.getRole().name().equals("ADMIN");

        boolean isOwner =
                material.getTeacher()
                        .getId()
                        .equals(user.getId());

        if (!isAdmin && !isOwner) {

            throw new AccessDeniedException(
                    "You can only update your own course materials"
            );
        }

        Section section =
                sectionRepository.findById(
                        request.getSectionId()
                ).orElseThrow(() ->
                        new RuntimeException(
                                "Section not found"
                        ));

        Subject subject =
                subjectRepository.findById(
                        request.getSubjectId()
                ).orElseThrow(() ->
                        new RuntimeException(
                                "Subject not found"
                        ));

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

        material.setTitle(
                request.getTitle()
        );

        material.setDescription(
                request.getDescription()
        );

        material.setSection(section);
        material.setSubject(subject);

        material.setUpdatedAt(
                LocalDateTime.now()
        );

        return toResponse(
                courseMaterialRepository.save(material)
        );
    }

    // ---------------------------------------------------------
    // DELETE
    // ---------------------------------------------------------

    public void deleteMaterial(
            UUID materialId,
            String userEmail
    ) {

        CourseMaterial material =
                courseMaterialRepository.findById(materialId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Course material not found"
                                ));

        User user =
                userRepository.findByEmail(userEmail)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "User not found"
                                ));

        boolean isAdmin =
                user.getRole().name().equals("ADMIN");

        boolean isOwner =
                material.getTeacher()
                        .getId()
                        .equals(user.getId());

        if (!isAdmin && !isOwner) {

            throw new AccessDeniedException(
                    "You can only delete your own course materials"
            );
        }

        if (material.getAttachmentPath() != null) {

            courseMaterialFileService.deleteFile(
                    material.getAttachmentPath()
            );
        }

        courseMaterialRepository.delete(material);
    }

    // ---------------------------------------------------------
    // UPLOAD ATTACHMENT
    // ---------------------------------------------------------

    public void uploadAttachment(
            UUID materialId,
            MultipartFile file,
            String userEmail
    ) {

        CourseMaterial material =
                courseMaterialRepository.findById(materialId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Course material not found"
                                ));

        User user =
                userRepository.findByEmail(userEmail)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "User not found"
                                ));

        boolean isAdmin =
                user.getRole().name().equals("ADMIN");

        boolean isOwner =
                material.getTeacher()
                        .getId()
                        .equals(user.getId());

        if (!isAdmin && !isOwner) {

            throw new AccessDeniedException(
                    "You can only upload attachments to your own course materials"
            );
        }

        if (!isAdmin) {

            validateTeacherAccess(
                    user,
                    material.getSection(),
                    material.getSubject()
            );
        }

        if (file == null || file.isEmpty()) {

            throw new RuntimeException(
                    "Attachment file is required"
            );
        }

        if (material.getAttachmentPath() != null) {

            courseMaterialFileService.deleteFile(
                    material.getAttachmentPath()
            );
        }

        String path =
                courseMaterialFileService.saveFile(file);

        material.setAttachmentPath(path);

        material.setUpdatedAt(
                LocalDateTime.now()
        );

        courseMaterialRepository.save(material);
    }

    // ---------------------------------------------------------
    // DOWNLOAD ATTACHMENT
    // ---------------------------------------------------------

    public Resource downloadAttachment(
            UUID materialId,
            String userEmail
    ) {

        CourseMaterial material =
                courseMaterialRepository.findById(materialId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Course material not found"
                                ));

        validateViewAccess(
                material,
                userEmail
        );

        if (material.getAttachmentPath() == null) {

            throw new RuntimeException(
                    "Course material has no attachment"
            );
        }

        return courseMaterialFileService.loadFile(
                material.getAttachmentPath()
        );
    }

    // ---------------------------------------------------------
    // AUTHORIZATION
    // ---------------------------------------------------------

    private void validateViewAccess(
            CourseMaterial material,
            String userEmail
    ) {

        User user =
                userRepository.findByEmail(userEmail)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "User not found"
                                ));

        if (user.getRole().name().equals("ADMIN")) {
            return;
        }

        if (user.getRole().name().equals("TEACHER")) {

            boolean assigned =
                    teacherAssignmentRepository
                            .existsByTeacherIdAndSectionIdAndSubjectId(
                                    user.getId(),
                                    material.getSection().getId(),
                                    material.getSubject().getId()
                            );

            if (!assigned) {

                throw new AccessDeniedException(
                        "Teacher is not assigned to this subject and section"
                );
            }

            return;
        }

        if (user.getRole().name().equals("STUDENT")) {

            Student student =
                    studentRepository.findByUserEmail(
                            userEmail
                    );

            if (student == null) {

                throw new RuntimeException(
                        "Student profile not found"
                );
            }

            if (!student.getSection()
                    .getId()
                    .equals(material.getSection().getId())) {

                throw new AccessDeniedException(
                        "Course material does not belong to your section"
                );
            }

            return;
        }

        throw new AccessDeniedException(
                "You are not authorized to view this course material"
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

    // ---------------------------------------------------------
    // RESPONSE
    // ---------------------------------------------------------

    private CourseMaterialResponse toResponse(
            CourseMaterial material
    ) {

        return new CourseMaterialResponse(
                material.getId(),
                material.getTitle(),
                material.getDescription(),

                material.getSection().getId(),
                material.getSection().getName(),

                material.getSubject().getId(),
                material.getSubject().getName(),
                material.getSubject().getCode(),

                material.getTeacher().getId(),
                material.getTeacher().getEmail(),

                material.getAttachmentPath(),

                material.getCreatedAt(),
                material.getUpdatedAt()
        );
    }
}