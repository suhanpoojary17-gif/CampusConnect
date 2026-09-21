package com.campusconnect.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import com.campusconnect.dto.AttendanceAuditResponse;
import com.campusconnect.dto.AttendanceRequest;
import com.campusconnect.dto.AttendanceResponse;
import com.campusconnect.dto.AttendanceSummaryResponse;
import com.campusconnect.entity.Attendance;
import com.campusconnect.entity.User;
import com.campusconnect.repository.UserRepository;
import com.campusconnect.service.AttendanceService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.campusconnect.dto.AttendanceRiskResponse;
import com.campusconnect.dto.AttendanceForecastResponse;
import com.campusconnect.dto.SubjectAttendanceResponse;


import java.time.LocalDate;
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
            @RequestParam LocalDate attendanceDate,
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
            @PathVariable("id") UUID sectionId,
            Authentication authentication) {

        return ResponseEntity.ok(
                attendanceService.getClassAttendance(
                        sectionId,
                        authentication.getName()
                )
        );
    }

        @GetMapping("/student/{id}")
        public ResponseEntity<List<AttendanceResponse>> getStudentAttendance(
                @PathVariable("id") UUID studentId,
                Authentication authentication) {

        List<Attendance> attendanceList =
                attendanceService.getStudentAttendance(
                        studentId,
                        authentication.getName()
                );

        List<AttendanceResponse> responseList =
                attendanceList.stream()
                        .map(attendance -> new AttendanceResponse(
                                attendance.getId(),
                                attendance.getStudent().getId(),
                                attendance.getSubject().getId(),
                                attendance.getTeacher().getId(),
                                attendance.getAttendanceDate(),
                                attendance.getStatus()
                        ))
                        .toList();

        return ResponseEntity.ok(responseList);
        }

    @PreAuthorize("hasRole('TEACHER')")
    @PutMapping("/{id}")
    public ResponseEntity<AttendanceResponse> updateAttendance(
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

        // Convert entity to DTO
        AttendanceResponse response = new AttendanceResponse(
                attendance.getId(),
                attendance.getStudent().getId(),
                attendance.getSubject().getId(),
                attendance.getTeacher().getId(),
                attendance.getAttendanceDate(),
                attendance.getStatus()
        );

        return ResponseEntity.ok(response);
    }

        @GetMapping("/student/{studentId}/subject/{subjectId}/summary")
        public ResponseEntity<AttendanceSummaryResponse> getSubjectAttendanceSummary(
                @PathVariable UUID studentId,
                @PathVariable UUID subjectId,
                Authentication authentication) {

        return ResponseEntity.ok(
                attendanceService.getSubjectAttendanceSummary(
                        studentId,
                        subjectId,
                        authentication.getName()
                )
        );
        }

        @GetMapping("/student/{studentId}/summary")
        public ResponseEntity<AttendanceSummaryResponse> getOverallAttendanceSummary(
                @PathVariable UUID studentId,
                Authentication authentication) {

        return ResponseEntity.ok(
                attendanceService.getOverallAttendanceSummary(
                        studentId,
                        authentication.getName()
                )
        );
        }

        @GetMapping("/student/{studentId}/risk")
        public ResponseEntity<AttendanceRiskResponse> getAttendanceRisk(
                @PathVariable UUID studentId,
                Authentication authentication) {

        return ResponseEntity.ok(
                attendanceService.getAttendanceRisk(
                        studentId,
                        authentication.getName()
                )
        );
        }

        @GetMapping("/student/{studentId}/forecast")
        public ResponseEntity<AttendanceForecastResponse> getAttendanceForecast(
                @PathVariable UUID studentId,
                @RequestParam long upcomingClasses,
                Authentication authentication) {

        return ResponseEntity.ok(
                attendanceService.getAttendanceForecast(
                        studentId,
                        upcomingClasses,
                        authentication.getName()
                )
        );
        }

        @GetMapping("/student/{studentId}/subject/{subjectId}/risk")
        public ResponseEntity<AttendanceRiskResponse> getSubjectAttendanceRisk(
                @PathVariable UUID studentId,
                @PathVariable UUID subjectId,
                Authentication authentication) {

        return ResponseEntity.ok(
                attendanceService.getSubjectAttendanceRisk(
                        studentId,
                        subjectId,
                        authentication.getName()
                )
        );
        }

        @GetMapping("/student/{studentId}/subject/{subjectId}/forecast")
        public ResponseEntity<AttendanceForecastResponse> getSubjectAttendanceForecast(
                @PathVariable UUID studentId,
                @PathVariable UUID subjectId,
                @RequestParam long upcomingClasses,
                Authentication authentication) {

        return ResponseEntity.ok(
                attendanceService.getSubjectAttendanceForecast(
                        studentId,
                        subjectId,
                        upcomingClasses,
                        authentication.getName()
                )
        );
        }

        @GetMapping("/student/{studentId}/all-subjects")
        public ResponseEntity<List<SubjectAttendanceResponse>> getAllSubjectAttendance(
                @PathVariable UUID studentId,
                Authentication authentication) {

        return ResponseEntity.ok(
                attendanceService.getAllSubjectAttendance(
                        studentId,
                        authentication.getName()
                )
        );
        }

        @GetMapping("/{id}/audit")
        public ResponseEntity<List<AttendanceAuditResponse>> getAttendanceAuditHistory(
                @PathVariable("id") Long attendanceId,
                Authentication authentication) {

        return ResponseEntity.ok(
                attendanceService.getAttendanceAuditHistory(
                        attendanceId,
                        authentication.getName()
                )
        );
        }
}