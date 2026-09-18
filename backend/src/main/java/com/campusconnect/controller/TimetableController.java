package com.campusconnect.controller;

import com.campusconnect.dto.TimetableRequest;
import com.campusconnect.dto.TimetableResponse;
import com.campusconnect.entity.User;
import com.campusconnect.repository.UserRepository;
import com.campusconnect.service.TimetableService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/timetable")
public class TimetableController {

    private final TimetableService timetableService;
    private final UserRepository userRepository;

    public TimetableController(
            TimetableService timetableService,
            UserRepository userRepository
    ) {
        this.timetableService = timetableService;
        this.userRepository = userRepository;
    }

    // ADMIN - CREATE
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TimetableResponse> createTimetable(
            @RequestBody TimetableRequest request
    ) {
        return ResponseEntity.ok(
                timetableService.createTimetable(request)
        );
    }

    // ADMIN - VIEW ALL
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<TimetableResponse>> getAllTimetables() {
        return ResponseEntity.ok(
                timetableService.getAllTimetables()
        );
    }

    // ADMIN - UPDATE
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TimetableResponse> updateTimetable(
            @PathVariable Long id,
            @RequestBody TimetableRequest request
    ) {
        return ResponseEntity.ok(
                timetableService.updateTimetable(id, request)
        );
    }

    // ADMIN - DELETE
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteTimetable(
            @PathVariable Long id
    ) {
        timetableService.deleteTimetable(id);

        return ResponseEntity.noContent().build();
    }

    // STUDENT - TODAY
    @GetMapping("/today/{sectionId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<TimetableResponse>> getTodayTimetable(
            @PathVariable UUID sectionId,
            Authentication authentication
    ) {
        String studentEmail = authentication.getName();

        return ResponseEntity.ok(
                timetableService.getTodayTimetable(
                        sectionId,
                        studentEmail
                )
        );
    }

    // STUDENT - WEEKLY
    @GetMapping("/weekly/{sectionId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<TimetableResponse>> getWeeklyTimetable(
            @PathVariable UUID sectionId,
            Authentication authentication
    ) {
        String studentEmail = authentication.getName();

        return ResponseEntity.ok(
                timetableService.getWeeklyTimetable(
                        sectionId,
                        studentEmail
                )
        );
    }

    // TEACHER - ASSIGNED TIMETABLE
    @GetMapping("/teacher")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<TimetableResponse>> getTeacherTimetable(
            Authentication authentication
    ) {
        String teacherEmail = authentication.getName();

        User teacher = userRepository.findByEmail(teacherEmail)
        .orElseThrow(() -> new RuntimeException("Teacher not found"));

        return ResponseEntity.ok(
                timetableService.getTeacherTimetable(
                        teacher.getId()
                )
        );
    }
}