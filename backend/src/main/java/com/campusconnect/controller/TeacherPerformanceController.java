package com.campusconnect.controller;

import com.campusconnect.dto.StudentPerformanceResponse;
import com.campusconnect.service.TeacherPerformanceService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.campusconnect.entity.User;
import com.campusconnect.repository.UserRepository;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/performance")
public class TeacherPerformanceController {

    private final TeacherPerformanceService teacherPerformanceService;
    private final UserRepository userRepository;

    public TeacherPerformanceController(
            TeacherPerformanceService teacherPerformanceService,
            UserRepository userRepository
    ) {

        this.teacherPerformanceService =
                teacherPerformanceService;

        this.userRepository =
                userRepository;
    }

    @GetMapping("/admin")
    public ResponseEntity<List<StudentPerformanceResponse>>
    getAdminStudentPerformance(
            @RequestParam UUID sectionId,
            @RequestParam UUID subjectId,
            Authentication authentication
    ) {

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found")
                );

        if (!user.getRole().name().equals("ADMIN")) {
            throw new RuntimeException(
                    "Only admins can access this performance view"
            );
        }

        return ResponseEntity.ok(
                teacherPerformanceService
                        .getAdminStudentPerformance(
                                sectionId,
                                subjectId
                        )
        );
    }    

}