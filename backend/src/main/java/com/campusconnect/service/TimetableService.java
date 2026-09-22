package com.campusconnect.service;

import com.campusconnect.dto.TimetableRequest;
import com.campusconnect.dto.TimetableResponse;
import com.campusconnect.entity.User;
import com.campusconnect.model.*;
import com.campusconnect.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class TimetableService {

    private final TimetableRepository timetableRepository;
    private final SectionRepository sectionRepository;
    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;
    private final TeacherAssignmentRepository teacherAssignmentRepository;
    private final StudentRepository studentRepository;

    public TimetableService(
            TimetableRepository timetableRepository,
            SectionRepository sectionRepository,
            SubjectRepository subjectRepository,
            UserRepository userRepository,
            TeacherAssignmentRepository teacherAssignmentRepository,
            StudentRepository studentRepository
    ) {
        this.timetableRepository = timetableRepository;
        this.sectionRepository = sectionRepository;
        this.subjectRepository = subjectRepository;
        this.userRepository = userRepository;
        this.teacherAssignmentRepository = teacherAssignmentRepository;
        this.studentRepository = studentRepository;
    }

    @Transactional
    public TimetableResponse createTimetable(TimetableRequest request) {
        validateTime(request);

        Section section = sectionRepository.findById(request.getSectionId())
                .orElseThrow(() -> new RuntimeException("Section not found"));

        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new RuntimeException("Subject not found"));

        User teacher = userRepository.findById(request.getTeacherId())
                .orElseThrow(() -> new RuntimeException("Teacher not found"));

        validateSubjectBelongsToSection(subject, section);
        validateTeacherAssignment(teacher.getId(), section.getId(), subject.getId());
        validateConflict(section.getId(), teacher.getId(), request, null);

        Timetable timetable = new Timetable();
        mapToEntity(timetable, request, section, subject, teacher);

        Timetable saved = timetableRepository.save(timetable);
        return convertToResponse(saved);
    }

    public List<TimetableResponse> getAllTimetables() {
        return timetableRepository.findAll()
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    // Student: View today's timetable
    public List<TimetableResponse> getTodayTimetable(
            UUID sectionId,
            String studentEmail
    ) {
        validateStudentSection(sectionId, studentEmail);

        DayOfWeek today =
                DayOfWeek.valueOf(LocalDate.now().getDayOfWeek().name());

        return timetableRepository
                .findBySectionIdAndDayOrderByStartTime(sectionId, today)
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

        // Student: View tomorrow's timetable
        public List<TimetableResponse> getTomorrowTimetable(
                UUID sectionId,
                String studentEmail
        ) {
        validateStudentSection(sectionId, studentEmail);

        DayOfWeek tomorrow =
                DayOfWeek.valueOf(
                        LocalDate.now()
                                .plusDays(1)
                                .getDayOfWeek()
                                .name()
                );

        return timetableRepository
                .findBySectionIdAndDayOrderByStartTime(
                        sectionId,
                        tomorrow
                )
                .stream()
                .map(this::convertToResponse)
                .toList();
        }

    // Student: View weekly timetable
    public List<TimetableResponse> getWeeklyTimetable(
            UUID sectionId,
            String studentEmail
    ) {
        validateStudentSection(sectionId, studentEmail);

        return timetableRepository
                .findBySectionIdOrderByDayAscStartTimeAsc(sectionId)
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    // Teacher: View assigned timetable
    public List<TimetableResponse> getTeacherTimetable(Long teacherId) {
        return timetableRepository
                .findByTeacherIdOrderByDayAscStartTimeAsc(teacherId)
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    @Transactional
    public TimetableResponse updateTimetable(
            Long id,
            TimetableRequest request
    ) {
        validateTime(request);

        Timetable timetable = timetableRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Timetable not found"));

        Section section = sectionRepository.findById(request.getSectionId())
                .orElseThrow(() -> new RuntimeException("Section not found"));

        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new RuntimeException("Subject not found"));

        User teacher = userRepository.findById(request.getTeacherId())
                .orElseThrow(() -> new RuntimeException("Teacher not found"));

        validateSubjectBelongsToSection(subject, section);
        validateTeacherAssignment(
                teacher.getId(),
                section.getId(),
                subject.getId()
        );

        validateConflict(
                section.getId(),
                teacher.getId(),
                request,
                id
        );

        mapToEntity(
                timetable,
                request,
                section,
                subject,
                teacher
        );

        Timetable updated = timetableRepository.save(timetable);

        return convertToResponse(updated);
    }

    @Transactional
    public void deleteTimetable(Long id) {
        if (!timetableRepository.existsById(id)) {
            throw new RuntimeException("Timetable not found");
        }

        timetableRepository.deleteById(id);
    }

    // ---------------------------------------------------------
    // Student Authorization
    // ---------------------------------------------------------

    private void validateStudentSection(
            UUID sectionId,
            String studentEmail
    ) {
        Student student = studentRepository.findByUserEmail(studentEmail);

        if (student == null) {
            throw new RuntimeException("Student not found");
        }

        if (student.getSection() == null) {
            throw new RuntimeException("Student is not assigned to a section");
        }

        if (!student.getSection().getId().equals(sectionId)) {
            throw new RuntimeException(
                    "You are not authorized to view this section timetable"
            );
        }
    }

    // ---------------------------------------------------------
    // Validation
    // ---------------------------------------------------------

    private void validateTime(TimetableRequest request) {
        if (request.getStartTime() == null ||
                request.getEndTime() == null) {

            throw new RuntimeException(
                    "Start time and end time are required"
            );
        }

        if (!request.getStartTime().isBefore(request.getEndTime())) {
            throw new RuntimeException(
                    "Start time must be before end time"
            );
        }
    }

    private void validateSubjectBelongsToSection(
            Subject subject,
            Section section
    ) {
        if (!subject.getSection().getId().equals(section.getId())) {
            throw new RuntimeException(
                    "Subject does not belong to this section"
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
                    "Teacher is not assigned to this subject and section"
            );
        }
    }

    // ---------------------------------------------------------
    // Timetable Conflict Validation
    // ---------------------------------------------------------

    private void validateConflict(
            UUID sectionId,
            Long teacherId,
            TimetableRequest request,
            Long timetableId
    ) {

        boolean sectionConflict =
                timetableId == null
                        ? timetableRepository.existsConflict(
                                sectionId,
                                request.getDay(),
                                request.getStartTime(),
                                request.getEndTime()
                        )
                        : timetableRepository.existsConflictExceptId(
                                sectionId,
                                request.getDay(),
                                request.getStartTime(),
                                request.getEndTime(),
                                timetableId
                        );

        if (sectionConflict) {
            throw new RuntimeException(
                    "Section conflict: another class is already scheduled during this time"
            );
        }

        boolean teacherConflict =
                timetableId == null
                        ? timetableRepository.existsTeacherConflict(
                                teacherId,
                                request.getDay(),
                                request.getStartTime(),
                                request.getEndTime()
                        )
                        : timetableRepository.existsTeacherConflictExceptId(
                                teacherId,
                                request.getDay(),
                                request.getStartTime(),
                                request.getEndTime(),
                                timetableId
                        );

        if (teacherConflict) {
            throw new RuntimeException(
                    "Teacher conflict: teacher is already assigned to another class during this time"
            );
        }

        if (request.getRoom() != null &&
                !request.getRoom().isBlank()) {

            boolean roomConflict =
                    timetableId == null
                            ? timetableRepository.existsRoomConflict(
                                    request.getRoom(),
                                    request.getDay(),
                                    request.getStartTime(),
                                    request.getEndTime()
                            )
                            : timetableRepository.existsRoomConflictExceptId(
                                    request.getRoom(),
                                    request.getDay(),
                                    request.getStartTime(),
                                    request.getEndTime(),
                                    timetableId
                            );

            if (roomConflict) {
                throw new RuntimeException(
                        "Room conflict: room is already booked during this time"
                );
            }
        }
    }

    // ---------------------------------------------------------
    // Entity Mapping
    // ---------------------------------------------------------

    private void mapToEntity(
            Timetable timetable,
            TimetableRequest request,
            Section section,
            Subject subject,
            User teacher
    ) {
        timetable.setSection(section);
        timetable.setDay(request.getDay());
        timetable.setStartTime(request.getStartTime());
        timetable.setEndTime(request.getEndTime());
        timetable.setSubject(subject);
        timetable.setTeacher(teacher);
        timetable.setRoom(request.getRoom());
    }

    // ---------------------------------------------------------
    // Response Conversion
    // ---------------------------------------------------------

    private TimetableResponse convertToResponse(
            Timetable timetable
    ) {
        return new TimetableResponse(
                timetable.getId(),
                timetable.getSection().getId(),
                timetable.getDay(),
                timetable.getStartTime(),
                timetable.getEndTime(),
                timetable.getSubject().getId(),
                timetable.getTeacher().getId(),
                timetable.getRoom()
        );
    }
}
