package com.campusconnect.controller;

import com.campusconnect.dto.AcademicPerformanceResponse;
import com.campusconnect.service.AcademicPerformanceService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/performance")
public class AcademicPerformanceController {

    private final AcademicPerformanceService academicPerformanceService;

    public AcademicPerformanceController(
            AcademicPerformanceService academicPerformanceService
    ) {
        this.academicPerformanceService =
                academicPerformanceService;
    }

    @GetMapping("/my")
    public ResponseEntity<AcademicPerformanceResponse> getMyPerformance(
            Authentication authentication
    ) {

        String email = authentication.getName();

        return ResponseEntity.ok(
                academicPerformanceService.getMyPerformance(
                        email
                )
        );
    }
}