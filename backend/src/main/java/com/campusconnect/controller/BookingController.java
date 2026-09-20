package com.campusconnect.controller;

import com.campusconnect.dto.BookingRequest;
import com.campusconnect.dto.BookingResponse;
import com.campusconnect.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    // Teacher: Create booking request
    @PostMapping
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<BookingResponse> createBooking(
            @Valid @RequestBody BookingRequest request,
            Authentication authentication) {

        BookingResponse response = bookingService.createBooking(
                request,
                authentication.getName()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // Teacher: View own bookings
    @GetMapping("/my")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<BookingResponse>> getMyBookings(
            Authentication authentication) {

        return ResponseEntity.ok(
                bookingService.getMyBookings(
                        authentication.getName()
                )
        );
    }

    // Teacher: Cancel own booking
    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<BookingResponse> cancelBooking(
            @PathVariable UUID id,
            Authentication authentication) {

        return ResponseEntity.ok(
                bookingService.cancelBooking(
                        id,
                        authentication.getName()
                )
        );
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<BookingResponse>> getAllBookings() {

        return ResponseEntity.ok(
                bookingService.getAllBookings()
        );
    }

    @GetMapping("/availability")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<BookingResponse>> getAvailability(
            @RequestParam UUID facilityId,
            @RequestParam LocalDate date) {

        return ResponseEntity.ok(
                bookingService.getAvailability(facilityId, date)
        );
    }
}