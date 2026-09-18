package com.campusconnect.service;

import com.campusconnect.dto.AttendanceRequest;
import com.campusconnect.entity.Attendance;
import com.campusconnect.entity.AttendanceStatus;
import com.campusconnect.entity.User;
import com.campusconnect.model.Student;
import com.campusconnect.model.Subject;
import com.campusconnect.repository.AttendanceRepository;
import com.campusconnect.repository.StudentRepository;
import com.campusconnect.repository.SubjectRepository;
import com.campusconnect.repository.TeacherAssignmentRepository;
import com.campusconnect.repository.UserRepository;
import org.springframework.stereotype.Service;
import com.campusconnect.dto.AttendanceSummaryResponse;
import com.campusconnect.entity.AttendanceStatus;

@Service
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final TeacherAssignmentRepository teacherAssignmentRepository;
    private final SubjectRepository subjectRepository;
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;

    public AttendanceService(
            AttendanceRepository attendanceRepository,
            TeacherAssignmentRepository teacherAssignmentRepository,
            SubjectRepository subjectRepository,
            StudentRepository studentRepository,
            UserRepository userRepository) {

        this.attendanceRepository = attendanceRepository;
        this.teacherAssignmentRepository = teacherAssignmentRepository;
        this.subjectRepository = subjectRepository;
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
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

    public java.util.List<Attendance> getClassAttendance(
        java.util.UUID sectionId) {

    return attendanceRepository.findBySubjectSectionId(sectionId);
}
    public java.util.List<Attendance> getStudentAttendance(
        java.util.UUID studentId) {

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

    java.util.UUID sectionId = subject.getSection().getId();

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

    // 4. Verify the student belongs to this section
    Student student = attendance.getStudent();

    if (!student.getSection().getId().equals(sectionId)) {
        throw new RuntimeException(
                "Student does not belong to this section"
        );
    }

    // 5. Update attendance status
    attendance.setStatus(request.getStatus());

    // 6. Update date if required
    attendance.setAttendanceDate(request.getAttendanceDate());

    // 7. Save updated attendance
    return attendanceRepository.save(attendance);
}
    public AttendanceSummaryResponse getSubjectAttendanceSummary(
        java.util.UUID studentId,
        java.util.UUID subjectId) {

    java.util.List<Attendance> records =
            attendanceRepository.findByStudentIdAndSubjectId(
                    studentId,
                    subjectId
            );

    long totalClasses = records.size();

    long presentClasses = records.stream()
            .filter(attendance ->
                    attendance.getStatus()
                            == com.campusconnect.entity.AttendanceStatus.PRESENT)
            .count();

    long absentClasses = records.stream()
            .filter(attendance ->
                    attendance.getStatus()
                            == com.campusconnect.entity.AttendanceStatus.ABSENT)
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
        java.util.UUID studentId) {

    java.util.List<Attendance> records =
            attendanceRepository.findByStudentId(studentId);

    long totalClasses = records.size();

    long presentClasses = records.stream()
            .filter(attendance ->
                    attendance.getStatus()
                            == com.campusconnect.entity.AttendanceStatus.PRESENT)
            .count();

    long absentClasses = records.stream()
            .filter(attendance ->
                    attendance.getStatus()
                            == com.campusconnect.entity.AttendanceStatus.ABSENT)
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

    public void markAllPresent(
        java.util.UUID sectionId,
        java.util.UUID subjectId,
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
    java.util.List<Student> students =
            studentRepository.findBySectionId(sectionId);

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
            attendance.setStatus(
                    AttendanceStatus.PRESENT
            );

            attendanceRepository.save(attendance);
        }
    }
}
}