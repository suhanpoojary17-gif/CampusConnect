package com.campusconnect.controller;

import com.campusconnect.dto.BusPassApplicationRequest;
import com.campusconnect.dto.BusPassResponse;
import com.campusconnect.entity.User;
import com.campusconnect.repository.UserRepository;
import com.campusconnect.service.BusPassService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bus-passes")
public class BusPassController {

    private final BusPassService busPassService;
    private final UserRepository userRepository;

    public BusPassController(BusPassService busPassService,
                             UserRepository userRepository) {
        this.busPassService = busPassService;
        this.userRepository = userRepository;
    }

    // Student applies for a bus pass
    @PostMapping
    public ResponseEntity<BusPassResponse> applyForBusPass(
            @RequestBody BusPassApplicationRequest request,
            Authentication authentication) {

        String email = authentication.getName();

        User student = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("Student not found"));

        BusPassResponse response =
                busPassService.applyForBusPass(
                        student.getId(),
                        request
                );

        return ResponseEntity.ok(response);
    }

    // Student views their own bus passes
    @GetMapping("/my")
    public ResponseEntity<List<BusPassResponse>> getMyBusPasses(
            Authentication authentication) {

        String email = authentication.getName();

        User student = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("Student not found"));

        List<BusPassResponse> passes =
                busPassService.getStudentPasses(
                        student.getId()
                );

        return ResponseEntity.ok(passes);
    }
}