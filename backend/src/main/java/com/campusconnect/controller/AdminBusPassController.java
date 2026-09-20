package com.campusconnect.controller;

import com.campusconnect.dto.BusPassResponse;
import com.campusconnect.entity.BusPass;
import com.campusconnect.model.BusPassStatus;
import com.campusconnect.repository.BusPassRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/bus-passes")
public class AdminBusPassController {

    private final BusPassRepository busPassRepository;

    public AdminBusPassController(BusPassRepository busPassRepository) {
        this.busPassRepository = busPassRepository;
    }

    // Admin views all bus pass applications
    @GetMapping
    public ResponseEntity<List<BusPassResponse>> getAllBusPasses() {

        List<BusPassResponse> passes =
                busPassRepository.findAll()
                        .stream()
                        .map(this::convertToResponse)
                        .collect(Collectors.toList());

        return ResponseEntity.ok(passes);
    }

    // Admin approves a bus pass
    @PutMapping("/{id}/approve")
    public ResponseEntity<BusPassResponse> approveBusPass(
            @PathVariable UUID id) {

        BusPass busPass = busPassRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Bus pass not found"));

        busPass.setStatus(BusPassStatus.APPROVED);

        BusPass savedPass = busPassRepository.save(busPass);

        return ResponseEntity.ok(convertToResponse(savedPass));
    }

    // Admin rejects a bus pass
    @PutMapping("/{id}/reject")
    public ResponseEntity<BusPassResponse> rejectBusPass(
            @PathVariable UUID id) {

        BusPass busPass = busPassRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Bus pass not found"));

        busPass.setStatus(BusPassStatus.REJECTED);

        BusPass savedPass = busPassRepository.save(busPass);

        return ResponseEntity.ok(convertToResponse(savedPass));
    }

    private BusPassResponse convertToResponse(BusPass busPass) {

        BusPassResponse response = new BusPassResponse();

        response.setId(busPass.getId());

        if (busPass.getStudent() != null) {
            response.setStudentId(
                    busPass.getStudent().getId()
            );

            response.setStudentName(
                    busPass.getStudent().getEmail()
            );
        }

        response.setRoute(busPass.getRoute());
        response.setStartDate(busPass.getStartDate());
        response.setExpiryDate(busPass.getExpiryDate());
        response.setPaymentReceiptNo(
                busPass.getPaymentReceiptNo()
        );
        response.setPaymentDate(
                busPass.getPaymentDate()
        );
        response.setPaymentAmount(
                busPass.getPaymentAmount()
        );
        response.setStatus(
                busPass.getStatus()
        );

        return response;
    }
}