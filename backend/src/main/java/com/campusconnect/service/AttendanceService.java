package com.campusconnect.service;

import com.campusconnect.dto.AttendanceAuditResponse;
import com.campusconnect.dto.AttendanceRequest;
import com.campusconnect.dto.AttendanceSummaryResponse;
import com.campusconnect.entity.Attendance;
import com.campusconnect.entity.AttendanceAudit;
import com.campusconnect.entity.AttendanceStatus;
import com.campusconnect.entity.Role;
import com.campusconnect.entity.User;
import com.campusconnect.model.Student;
import com.campusconnect.model.Subject;
import com.campusconnect.repository.AttendanceAuditRepository;
import com.campusconnect.repository.AttendanceRepository;
import com.campusconnect.repository.StudentRepository;
import com.campusconnect.repository.SubjectRepository;
import com.campusconnect.repository.TeacherAssignmentRepository;
import com.campusconnect.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final TeacherAssignmentRepository teacherAssignmentRepository;
    private final SubjectRepository subjectRepository;
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final AttendanceAuditRepository attendanceAuditRepository;

    public AttendanceService(
            AttendanceRepository attendanceRepository,
            TeacherAssignmentRepository teacherAssignmentRepository,
            SubjectRepository subjectRepository,
            StudentRepository studentRepository,
            UserRepository userRepository,
            AttendanceAuditRepository attendanceAuditRepository) {

        this.attendanceRepository = attendanceRepository;
        this.teacherAssignmentRepository = teacherAssignmentRepository;
        this.subjectRepository = subjectRepository;
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
        this.attendanceAuditRepository = attendanceAuditRepository;
    }

    public Attendance markAttendance(
            AttendanceRequest request,
            Long teacherId) {

        // 1. Verify that the subject belongs to the section
        boolean subjectBelongsToSection =
                subjectRepository.existsByIdAndSectionId(
                        request.getSubjectId(),
                        request.getSectionId()
                );

        if (!subjectBelongsToSection) {
            throw new RuntimeException(
                    "Subject does not belong to the selected section"
            );
        }

        // 2. Verify teacher assignment
        boolean teacherAssigned =
                teacherAssignmentRepository
                        .existsByTeacherIdAndSectionIdAndSubjectId(
                                teacherId,
                                request.getSectionId(),
                                request.getSubjectId()
                        );

        if (!teacherAssigned) {
            throw new RuntimeException(
                    "Teacher is not assigned to this subject/class"
            );
        }

        // 3. Find student
        Student student = studentRepository
                .findById(request.getStudentId())
                .orElseThrow(() ->
                        new RuntimeException("Student not found")
                );

        // 4. Verify student belongs to the selected section
        if (!student.getSection().getId()
                .equals(request.getSectionId())) {

            throw new RuntimeException(
                    "Student does not belong to the selected section"
            );
        }

        // 5. Check duplicate attendance
        boolean alreadyExists =
                attendanceRepository
                        .existsByStudentIdAndSubjectIdAndAttendanceDate(
                                request.getStudentId(),
                                request.getSubjectId(),
                                request.getAttendanceDate()
                        );

        if (alreadyExists) {
            throw new RuntimeException(
                    "Attendance already exists for this student on this date"
            );
        }

        // 6. Find teacher
        User teacher = userRepository
                .findById(teacherId)
                .orElseThrow(() ->
                        new RuntimeException("Teacher not found")
                );

        // 7. Find subject
        Subject subject = subjectRepository
                .findById(request.getSubjectId())
                .orElseThrow(() ->
                        new RuntimeException("Subject not found")
                );

        // 8. Create attendance
        Attendance attendance = new Attendance();

        attendance.setStudent(student);
        attendance.setSubject(subject);
        attendance.setTeacher(teacher);
        attendance.setAttendanceDate(request.getAttendanceDate());
        attendance.setStatus(request.getStatus());

        // 9. Save attendance
        return attendanceRepository.save(attendance);
    }

    public List<Attendance> getClassAttendance(
            UUID sectionId,
            String requesterEmail) {

        User requester = userRepository
                .findByEmail(requesterEmail)
                .orElseThrow(() ->
                        new RuntimeException("User not found")
                );

        // Only teachers and admins can access class attendance
        if (requester.getRole() == Role.TEACHER) {

            boolean hasAssignment =
                    teacherAssignmentRepository
                            .existsByTeacherIdAndSectionId(
                                    requester.getId(),
                                    sectionId
                            );

            if (!hasAssignment) {
                throw new RuntimeException(
                        "Teacher is not assigned to this section"
                );
            }
        }

        return attendanceRepository.findBySubjectSectionId(sectionId);
    }

    public List<Attendance> getStudentAttendance(
            UUID studentId,
            String requesterEmail) {

        User requester = userRepository
                .findByEmail(requesterEmail)
                .orElseThrow(() ->
                        new RuntimeException("User not found")
                );

        // Students can only view their own attendance
        if (requester.getRole() == Role.STUDENT) {

            Student student =
                    studentRepository.findByUserEmail(requesterEmail);

            if (student == null) {
                throw new RuntimeException(
                        "Student profile not found"
                );
            }

            if (!student.getId().equals(studentId)) {
                throw new RuntimeException(
                        "Students can only view their own attendance"
                );
            }
        }

        return attendanceRepository.findByStudentId(studentId);
    }

    public Attendance updateAttendance(
            Long attendanceId,
            AttendanceRequest request,
            Long teacherId) {

        // 1. Find existing attendance
        Attendance attendance = attendanceRepository
                .findById(attendanceId)
                .orElseThrow(() ->
                        new RuntimeException("Attendance not found")
                );

        // 2. Get the subject's section
        Subject subject = attendance.getSubject();
        UUID sectionId = subject.getSection().getId();

        // 3. Verify teacher is assigned to this section + subject
        boolean teacherAssigned =
                teacherAssignmentRepository
                        .existsByTeacherIdAndSectionIdAndSubjectId(
                                teacherId,
                                sectionId,
                                subject.getId()
                        );

        if (!teacherAssigned) {
            throw new RuntimeException(
                    "Teacher is not assigned to this subject/class"
            );
        }

        // 4. Verify student belongs to this section
        Student student = attendance.getStudent();

        if (!student.getSection().getId().equals(sectionId)) {
            throw new RuntimeException(
                    "Student does not belong to this section"
            );
        }

        // 5. Find teacher making the change
        User teacher = userRepository
                .findById(teacherId)
                .orElseThrow(() ->
                        new RuntimeException("Teacher not found")
                );

        // 6. Store the old status
        AttendanceStatus oldStatus = attendance.getStatus();

        // 7. Get the new status
        AttendanceStatus newStatus = request.getStatus();

        // 8. Create audit record only if status actually changes
        if (oldStatus != newStatus) {

            AttendanceAudit audit = new AttendanceAudit();

            audit.setAttendance(attendance);
            audit.setChangedBy(teacher);
            audit.setOldStatus(oldStatus);
            audit.setNewStatus(newStatus);
            audit.setChangedAt(LocalDateTime.now());

            attendanceAuditRepository.save(audit);
        }

        // 9. Update attendance
        attendance.setStatus(newStatus);

        // 10. Update date if required
        attendance.setAttendanceDate(request.getAttendanceDate());

        // 11. Save updated attendance
        return attendanceRepository.save(attendance);
    }

    public AttendanceSummaryResponse getSubjectAttendanceSummary(
            UUID studentId,
            UUID subjectId,
            String requesterEmail) {

        User requester = userRepository
                .findByEmail(requesterEmail)
                .orElseThrow(() ->
                        new RuntimeException("User not found")
                );

        // Students can only view their own subject attendance
        if (requester.getRole() == Role.STUDENT) {

            Student student =
                    studentRepository.findByUserEmail(requesterEmail);

            if (student == null) {
                throw new RuntimeException(
                        "Student profile not found"
                );
            }

            if (!student.getId().equals(studentId)) {
                throw new RuntimeException(
                        "Students can only view their own attendance"
                );
            }
        }

        List<Attendance> records =
                attendanceRepository.findByStudentIdAndSubjectId(
                        studentId,
                        subjectId
                );

        long totalClasses = records.size();

        long presentClasses = records.stream()
                .filter(attendance ->
                        attendance.getStatus() == AttendanceStatus.PRESENT)
                .count();

        long absentClasses = records.stream()
                .filter(attendance ->
                        attendance.getStatus() == AttendanceStatus.ABSENT)
                .count();

        double attendancePercentage =
                totalClasses == 0
                        ? 0.0
                        : (presentClasses * 100.0) / totalClasses;

        return new AttendanceSummaryResponse(
                totalClasses,
                presentClasses,
                absentClasses,
                attendancePercentage
        );
    }

    public AttendanceSummaryResponse getOverallAttendanceSummary(
            UUID studentId,
            String requesterEmail) {

        User requester = userRepository
                .findByEmail(requesterEmail)
                .orElseThrow(() ->
                        new RuntimeException("User not found")
                );

        if (requester.getRole() == Role.STUDENT) {

            Student student =
                    studentRepository.findByUserEmail(requesterEmail);

            if (student == null) {
                throw new RuntimeException(
                        "Student profile not found"
                );
            }

            if (!student.getId().equals(studentId)) {
                throw new RuntimeException(
                        "Students can only view their own attendance"
                );
            }
        }

        List<Attendance> attendanceList =
                attendanceRepository.findByStudentId(studentId);

        int totalClasses = attendanceList.size();

        int presentClasses = (int) attendanceList.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.PRESENT)
                .count();

        int absentClasses = totalClasses - presentClasses;

        double percentage = totalClasses == 0
                ? 0.0
                : ((double) presentClasses / totalClasses) * 100;

        return new AttendanceSummaryResponse(
                totalClasses,
                presentClasses,
                absentClasses,
                percentage
        );
    }

    public void markAllPresent(
            UUID sectionId,
            UUID subjectId,
            java.time.LocalDate attendanceDate,
            Long teacherId) {

        // Check that the subject belongs to the section
        boolean subjectBelongsToSection =
                subjectRepository.existsByIdAndSectionId(
                        subjectId,
                        sectionId
                );

        if (!subjectBelongsToSection) {
            throw new RuntimeException(
                    "Subject does not belong to the selected section"
            );
        }

        // Check teacher assignment
        boolean teacherAssigned =
                teacherAssignmentRepository
                        .existsByTeacherIdAndSectionIdAndSubjectId(
                                teacherId,
                                sectionId,
                                subjectId
                        );

        if (!teacherAssigned) {
            throw new RuntimeException(
                    "Teacher is not assigned to this subject/class"
            );
        }

        // Get all students in the section
        List<Student> students = studentRepository.findBySectionId(sectionId);

        User teacher = userRepository
                .findById(teacherId)
                .orElseThrow(() ->
                        new RuntimeException("Teacher not found")
                );

        Subject subject = subjectRepository
                .findById(subjectId)
                .orElseThrow(() ->
                        new RuntimeException("Subject not found")
                );

        // Mark each student present
        for (Student student : students) {

            boolean alreadyExists =
                    attendanceRepository
                            .existsByStudentIdAndSubjectIdAndAttendanceDate(
                                    student.getId(),
                                    subjectId,
                                    attendanceDate
                            );

            if (!alreadyExists) {

                Attendance attendance = new Attendance();

                attendance.setStudent(student);
                attendance.setSubject(subject);
                attendance.setTeacher(teacher);
                attendance.setAttendanceDate(attendanceDate);
                attendance.setStatus(AttendanceStatus.PRESENT);

                attendanceRepository.save(attendance);
            }
        }
    }

        public List<AttendanceAuditResponse> getAttendanceAuditHistory(
                Long attendanceId,
                String requesterEmail) {

        Attendance attendance = attendanceRepository
                .findById(attendanceId)
                .orElseThrow(() ->
                        new RuntimeException("Attendance not found")
                );

        User requester = userRepository
                .findByEmail(requesterEmail)
                .orElseThrow(() ->
                        new RuntimeException("User not found")
                );

        // Teachers can only view audit history
        // for subjects/classes they are assigned to
        if (requester.getRole() == Role.TEACHER) {

                UUID sectionId =
                        attendance.getSubject()
                                .getSection()
                                .getId();

                boolean teacherAssigned =
                        teacherAssignmentRepository
                                .existsByTeacherIdAndSectionIdAndSubjectId(
                                        requester.getId(),
                                        sectionId,
                                        attendance.getSubject().getId()
                                );

                if (!teacherAssigned) {
                throw new RuntimeException(
                        "Teacher is not assigned to this subject/class"
                );
                }
        }

        // Students can only view audit history
        // for their own attendance
        if (requester.getRole() == Role.STUDENT) {

                Student student =
                        studentRepository.findByUserEmail(requesterEmail);

                if (student == null) {
                throw new RuntimeException(
                        "Student profile not found"
                );
                }

                if (!student.getId()
                        .equals(attendance.getStudent().getId())) {

                throw new RuntimeException(
                        "Students can only view their own attendance"
                );
                }
        }

        return attendanceAuditRepository
                .findByAttendanceIdOrderByChangedAtDesc(attendanceId)
                .stream()
                .map(audit -> new AttendanceAuditResponse(
                        audit.getId(),
                        audit.getAttendance().getId(),
                        audit.getChangedBy().getId(),
                        audit.getOldStatus(),
                        audit.getNewStatus(),
                        audit.getChangedAt()
                ))
                .toList();
        }
}