package com.campusconnect.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import com.campusconnect.dto.AttendanceRequest;
import com.campusconnect.entity.Attendance;
import com.campusconnect.entity.User;
import com.campusconnect.repository.UserRepository;
import com.campusconnect.service.AttendanceService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final UserRepository userRepository;

    public AttendanceController(
            AttendanceService attendanceService,
            UserRepository userRepository) {

        this.attendanceService = attendanceService;
        this.userRepository = userRepository;
    }

    @PreAuthorize("hasRole('TEACHER')")
    @PostMapping
    public ResponseEntity<Attendance> markAttendance(
            @Valid @RequestBody AttendanceRequest request,
            Authentication authentication) {

        // Get logged-in teacher's email from JWT
        String email = authentication.getName();

        // Find teacher in database
        User teacher = userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("Teacher not found")
                );

        // Mark attendance
        Attendance attendance =
                attendanceService.markAttendance(
                        request,
                        teacher.getId()
                );

        return ResponseEntity.ok(attendance);
    }

    
    @PreAuthorize("hasRole('TEACHER')")
    @PostMapping("/present-all")
    public ResponseEntity<String> markAllPresent(
            @RequestParam UUID sectionId,
            @RequestParam UUID subjectId,
            @RequestParam java.time.LocalDate attendanceDate,
            Authentication authentication) {

        String email = authentication.getName();

        User teacher = userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("Teacher not found")
                );

        attendanceService.markAllPresent(
                sectionId,
                subjectId,
                attendanceDate,
                teacher.getId()
        );

    return ResponseEntity.ok(
            "All students marked present successfully"
    );
}

    @GetMapping("/class/{id}")
    public ResponseEntity<List<Attendance>> getClassAttendance(
            @PathVariable("id") UUID sectionId) {

        return ResponseEntity.ok(
                attendanceService.getClassAttendance(sectionId)
        );
    }

    @GetMapping("/student/{id}")
    public ResponseEntity<java.util.List<Attendance>> getStudentAttendance(
        @PathVariable("id") java.util.UUID studentId) {

    return ResponseEntity.ok(
            attendanceService.getStudentAttendance(studentId)
    );
}

    @PreAuthorize("hasRole('TEACHER')")
    @PutMapping("/{id}")
    public ResponseEntity<Attendance> updateAttendance(
        @PathVariable("id") Long attendanceId,
        @Valid @RequestBody AttendanceRequest request,
        Authentication authentication) {

    // Get logged-in teacher's email from JWT
    String email = authentication.getName();

    // Find teacher
    User teacher = userRepository
            .findByEmail(email)
            .orElseThrow(() ->
                    new RuntimeException("Teacher not found")
            );

    // Update attendance
    Attendance attendance =
            attendanceService.updateAttendance(
                    attendanceId,
                    request,
                    teacher.getId()
            );

    return ResponseEntity.ok(attendance);
}
    @GetMapping("/student/{studentId}/subject/{subjectId}/summary")
    public ResponseEntity<com.campusconnect.dto.AttendanceSummaryResponse>
        getSubjectAttendanceSummary(
                @PathVariable UUID studentId,
                @PathVariable UUID subjectId) {

    return ResponseEntity.ok(
            attendanceService.getSubjectAttendanceSummary(
                    studentId,
                    subjectId
            )
    );
}

    @GetMapping("/student/{studentId}/summary")
    public ResponseEntity<com.campusconnect.dto.AttendanceSummaryResponse>
        getOverallAttendanceSummary(
                @PathVariable UUID studentId) {

    return ResponseEntity.ok(
            attendanceService.getOverallAttendanceSummary(studentId)
    );
}
}