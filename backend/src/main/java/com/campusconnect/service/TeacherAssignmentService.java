package com.campusconnect.service;

import com.campusconnect.entity.Role;
import com.campusconnect.entity.TeacherAssignment;
import com.campusconnect.entity.User;
import com.campusconnect.model.Section;
import com.campusconnect.model.Subject;
import com.campusconnect.repository.SectionRepository;
import com.campusconnect.repository.SubjectRepository;
import com.campusconnect.repository.TeacherAssignmentRepository;
import com.campusconnect.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class TeacherAssignmentService {

    private final TeacherAssignmentRepository assignmentRepository;
    private final UserRepository userRepository;
    private final SectionRepository sectionRepository;
    private final SubjectRepository subjectRepository;

    public TeacherAssignmentService(
            TeacherAssignmentRepository assignmentRepository,
            UserRepository userRepository,
            SectionRepository sectionRepository,
            SubjectRepository subjectRepository) {

        this.assignmentRepository = assignmentRepository;
        this.userRepository = userRepository;
        this.sectionRepository = sectionRepository;
        this.subjectRepository = subjectRepository;
    }

    public boolean isTeacherAssigned(
            String teacherEmail,
            UUID sectionId,
            UUID subjectId) {

        User teacher = userRepository
                .findByEmail(teacherEmail)
                .orElseThrow(() ->
                        new RuntimeException("Teacher not found")
                );

        return assignmentRepository
                .existsByTeacherIdAndSectionIdAndSubjectId(
                        teacher.getId(),
                        sectionId,
                        subjectId
                );
    }

    public TeacherAssignment createAssignment(
            Long teacherId,
            UUID sectionId,
            UUID subjectId) {

        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() ->
                        new RuntimeException("Teacher not found"));

        if (teacher.getRole() != Role.TEACHER) {
            throw new RuntimeException("Selected user is not a teacher");
        }

        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() ->
                        new RuntimeException("Section not found"));

        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() ->
                        new RuntimeException("Subject not found"));

        boolean alreadyAssigned =
                assignmentRepository
                        .existsByTeacherIdAndSectionIdAndSubjectId(
                                teacherId,
                                sectionId,
                                subjectId);

        if (alreadyAssigned) {
            throw new RuntimeException(
                    "Teacher is already assigned to this subject and section");
        }

        TeacherAssignment assignment =
                new TeacherAssignment(
                        teacher,
                        section,
                        subject);

        return assignmentRepository.save(assignment);
    }
}