package com.campusconnect.service;

import com.campusconnect.dto.NoticeRequest;
import com.campusconnect.dto.NoticeResponse;
import com.campusconnect.entity.User;
import com.campusconnect.model.Notice;
import com.campusconnect.model.Section;
import com.campusconnect.model.Student;
import com.campusconnect.repository.NoticeRepository;
import com.campusconnect.repository.SectionRepository;
import com.campusconnect.repository.StudentRepository;
import com.campusconnect.repository.TeacherAssignmentRepository;
import com.campusconnect.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;

import java.util.List;
import java.util.UUID;

@Service
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final SectionRepository sectionRepository;
    private final UserRepository userRepository;
    private final NoticeFileService noticeFileService;
    private final TeacherAssignmentRepository teacherAssignmentRepository;
    private final StudentRepository studentRepository;

    public NoticeService(
            NoticeRepository noticeRepository,
            SectionRepository sectionRepository,
            UserRepository userRepository,
            NoticeFileService noticeFileService,
            TeacherAssignmentRepository teacherAssignmentRepository,
            StudentRepository studentRepository
    ) {
        this.noticeRepository = noticeRepository;
        this.sectionRepository = sectionRepository;
        this.userRepository = userRepository;
        this.noticeFileService = noticeFileService;
        this.teacherAssignmentRepository = teacherAssignmentRepository;
        this.studentRepository = studentRepository;
    }

    public NoticeResponse createNotice(
            NoticeRequest request,
            String creatorEmail,
            MultipartFile file
    ) {

        Section section = sectionRepository.findById(request.getTargetSectionId())
                .orElseThrow(() -> new RuntimeException("Section not found"));

        User creator = userRepository.findByEmail(creatorEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (creator.getRole() == com.campusconnect.entity.Role.TEACHER) {

            boolean assigned = teacherAssignmentRepository
                    .existsByTeacherIdAndSectionId(
                            creator.getId(),
                            section.getId()
                    );

            if (!assigned) {
                throw new RuntimeException(
                        "Teacher is not assigned to this section"
                );
            }
        }

        Notice notice = new Notice();

        notice.setTitle(request.getTitle());
        notice.setDescription(request.getDescription());
        notice.setDate(request.getDate());
        notice.setTargetSection(section);
        notice.setCreator(creator);

        String attachmentPath = noticeFileService.saveFile(file);
        notice.setAttachmentPath(attachmentPath);

        Notice savedNotice = noticeRepository.save(notice);

        return convertToResponse(savedNotice);
    }

    public List<NoticeResponse> getAllNotices(String userEmail) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Notice> notices;

        if (user.getRole() == com.campusconnect.entity.Role.ADMIN) {

            notices = noticeRepository.findAll();

        } else if (user.getRole() == com.campusconnect.entity.Role.STUDENT) {

            Student student = studentRepository.findByUserEmail(userEmail);

            if (student == null) {
                throw new RuntimeException("Student profile not found");
            }

            notices = noticeRepository
                    .findByTargetSectionId(student.getSection().getId());

        } else {

            notices = noticeRepository.findAll();
        }

        return notices.stream()
                .map(this::convertToResponse)
                .toList();
    }

    public NoticeResponse getNoticeById(UUID id) {

        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notice not found"));

        return convertToResponse(notice);
    }

    public NoticeResponse updateNotice(
            UUID id,
            NoticeRequest request,
            String creatorEmail
    ) {

        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notice not found"));

        Section section = sectionRepository.findById(request.getTargetSectionId())
                .orElseThrow(() -> new RuntimeException("Section not found"));

        User creator = userRepository.findByEmail(creatorEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (creator.getRole() == com.campusconnect.entity.Role.TEACHER) {

            boolean assigned = teacherAssignmentRepository
                    .existsByTeacherIdAndSectionId(
                            creator.getId(),
                            section.getId()
                    );

            if (!assigned) {
                throw new RuntimeException(
                        "Teacher is not assigned to this section"
                );
            }

            if (!notice.getCreator().getId().equals(creator.getId())) {
                throw new RuntimeException(
                        "Teacher can only update their own notices"
                );
            }
        }

        notice.setTitle(request.getTitle());
        notice.setDescription(request.getDescription());
        notice.setDate(request.getDate());
        notice.setTargetSection(section);

        Notice updatedNotice = noticeRepository.save(notice);

        return convertToResponse(updatedNotice);
    }

    public NoticeResponse uploadAttachment(
        UUID noticeId,
        MultipartFile file,
        String userEmail
        ) {

        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new RuntimeException("Notice not found"));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRole() == com.campusconnect.entity.Role.TEACHER) {

                boolean assigned = teacherAssignmentRepository
                        .existsByTeacherIdAndSectionId(
                                user.getId(),
                                notice.getTargetSection().getId()
                        );

                if (!assigned) {
                throw new RuntimeException(
                        "Teacher is not assigned to this section"
                );
                }

                if (!notice.getCreator().getId().equals(user.getId())) {
                throw new RuntimeException(
                        "Teacher can only upload attachments to their own notices"
                );
                }
        }

        if (user.getRole() == com.campusconnect.entity.Role.STUDENT) {
        throw new RuntimeException(
                "Students cannot upload notice attachments"
        );
        }

        String attachmentPath = noticeFileService.saveFile(file);

        notice.setAttachmentPath(attachmentPath);

        Notice updatedNotice = noticeRepository.save(notice);

        return convertToResponse(updatedNotice);
        }

        public Resource downloadAttachment(
                UUID noticeId,
                String userEmail
        ) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new RuntimeException("Notice not found"));

        if (notice.getAttachmentPath() == null) {
                throw new RuntimeException("No attachment found");
        }

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // STUDENT: only their own section
        if (user.getRole() == com.campusconnect.entity.Role.STUDENT) {

                Student student = studentRepository.findByUserEmail(userEmail);

                if (student == null) {
                throw new RuntimeException("Student profile not found");
                }

                if (!student.getSection().getId()
                        .equals(notice.getTargetSection().getId())) {

                throw new RuntimeException(
                        "Student cannot access this notice"
                );
                }
        }

        // TEACHER: only assigned sections
        if (user.getRole() == com.campusconnect.entity.Role.TEACHER) {

                boolean assigned =
                        teacherAssignmentRepository
                                .existsByTeacherIdAndSectionId(
                                        user.getId(),
                                        notice.getTargetSection().getId()
                                );

                if (!assigned) {
                throw new RuntimeException(
                        "Teacher is not assigned to this section"
                );
                }
        }

        // ADMIN can access any notice
        return noticeFileService.loadFile(
                notice.getAttachmentPath()
        );
        }

    public void deleteNotice(UUID id, String creatorEmail) {

        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notice not found"));

        User creator = userRepository.findByEmail(creatorEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (creator.getRole() == com.campusconnect.entity.Role.TEACHER) {

            boolean assigned = teacherAssignmentRepository
                    .existsByTeacherIdAndSectionId(
                            creator.getId(),
                            notice.getTargetSection().getId()
                    );

            if (!assigned) {
                throw new RuntimeException(
                        "Teacher is not assigned to this section"
                );
            }

            if (!notice.getCreator().getId().equals(creator.getId())) {
                throw new RuntimeException(
                        "Teacher can only delete their own notices"
                );
            }
        }

        noticeRepository.delete(notice);
    }

    private NoticeResponse convertToResponse(Notice notice) {

        return new NoticeResponse(
                notice.getId(),
                notice.getTitle(),
                notice.getDescription(),
                notice.getDate(),
                notice.getTargetSection().getId(),
                notice.getTargetSection().getName(),
                notice.getCreator().getId(),
                notice.getCreator().getEmail(),
                notice.getAttachmentPath()
        );
    }
}