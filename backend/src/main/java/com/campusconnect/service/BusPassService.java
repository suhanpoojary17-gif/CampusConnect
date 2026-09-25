package com.campusconnect.service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import org.springframework.stereotype.Service;

import com.campusconnect.dto.BusPassApplicationRequest;
import com.campusconnect.dto.BusPassResponse;
import com.campusconnect.entity.BusPass;
import com.campusconnect.entity.Role;
import com.campusconnect.entity.User;
import com.campusconnect.model.BusPassStatus;
import com.campusconnect.repository.BusPassRepository;
import com.campusconnect.repository.UserRepository;

@Service
public class BusPassService {

    private final BusPassRepository busPassRepository;
    private final UserRepository userRepository;

    public BusPassService(BusPassRepository busPassRepository,
                          UserRepository userRepository) {
        this.busPassRepository = busPassRepository;
        this.userRepository = userRepository;
    }

    public BusPassResponse applyForBusPass(
            Long studentId,
            BusPassApplicationRequest request) {

        // Prevent duplicate active pass
        if (busPassRepository.existsByStudentIdAndStatus(
                studentId, BusPassStatus.APPROVED)) {

            throw new IllegalStateException(
                    "Student already has an active bus pass"
            );
        }

        // Prevent another pending application
        if (busPassRepository.existsByStudentIdAndStatus(
                studentId, BusPassStatus.PENDING)) {

            throw new IllegalStateException(
                    "Student already has a pending bus pass application"
            );
        }

        User student = userRepository.findById(studentId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Student not found"));

        // Make sure only students can apply
        if (student.getRole() != Role.STUDENT) {
            throw new IllegalArgumentException(
                    "Only students can apply for a bus pass"
            );
        }

        if (request.getStartDate() == null) {
            throw new IllegalArgumentException("Start date is required");
        }

        if (request.getStartDate().isBefore(LocalDate.now(ZoneId.systemDefault()))) {
            throw new IllegalArgumentException(
                    "Start date cannot be in the past"
            );
        }

        if (request.getRoute() == null ||
                request.getRoute().trim().isEmpty()) {

            throw new IllegalArgumentException("Route is required");
        }

        if (request.getPaymentReceiptNo() == null ||
                request.getPaymentReceiptNo().trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Payment receipt number is required"
            );
        }

        if (request.getPaymentDate() == null) {
            throw new IllegalArgumentException(
                    "Payment date is required"
            );
        }

        if (request.getPaymentAmount() == null ||
                request.getPaymentAmount().signum() <= 0) {

            throw new IllegalArgumentException(
                    "Valid payment amount is required"
            );
        }

        BusPass busPass = new BusPass();

        busPass.setStudent(student);
        busPass.setRoute(request.getRoute());
        busPass.setStartDate(request.getStartDate());

        // Yearly pass
        busPass.setExpiryDate(
                request.getStartDate().minusDays(1).plusYears(1)
        );

        busPass.setPaymentReceiptNo(
                request.getPaymentReceiptNo()
        );

        busPass.setPaymentDate(
                request.getPaymentDate()
        );

        busPass.setPaymentAmount(
                request.getPaymentAmount()
        );

        // Every new application starts as PENDING
        busPass.setStatus(BusPassStatus.PENDING);

        BusPass savedPass = busPassRepository.save(busPass);

        return convertToResponse(savedPass);
    }

    public List<BusPassResponse> getStudentPasses(Long studentId) {

        List<BusPass> passes =
                busPassRepository.findByStudentId(studentId);

        LocalDate today = LocalDate.now(ZoneId.systemDefault());

        for (BusPass pass : passes) {

            if (pass.getStatus() == BusPassStatus.APPROVED
                    && pass.getExpiryDate().isBefore(today)) {

                pass.setStatus(BusPassStatus.EXPIRED);
                busPassRepository.save(pass);
            }
        }

        return passes.stream()
                .map(this::convertToResponse)
                .toList();
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