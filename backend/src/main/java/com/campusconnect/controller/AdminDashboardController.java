package com.campusconnect.controller;

import com.campusconnect.dto.AdminDashboardResponse;
import com.campusconnect.service.AdminDashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.campusconnect.dto.AdminDashboardActivityResponse;

@RestController
@RequestMapping("/api/admin/dashboard")
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    public AdminDashboardController(
            AdminDashboardService adminDashboardService) {
        this.adminDashboardService = adminDashboardService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminDashboardResponse> getDashboard() {

        return ResponseEntity.ok(
                adminDashboardService.getDashboard()
        );
    }

    @GetMapping("/activity")
    public ResponseEntity<AdminDashboardActivityResponse> getActivity() {

        return ResponseEntity.ok(
                adminDashboardService.getActivity()
        );
    }
    

}