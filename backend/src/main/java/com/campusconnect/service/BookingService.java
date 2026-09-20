package com.campusconnect.service;

import com.campusconnect.dto.BookingRequest;
import com.campusconnect.dto.BookingResponse;
import com.campusconnect.entity.Booking;
import com.campusconnect.entity.BookingStatus;
import com.campusconnect.entity.Facility;
import com.campusconnect.entity.User;
import com.campusconnect.repository.BookingRepository;
import com.campusconnect.repository.FacilityRepository;
import com.campusconnect.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final FacilityRepository facilityRepository;
    private final UserRepository userRepository;

    public BookingService(
            BookingRepository bookingRepository,
            FacilityRepository facilityRepository,
            UserRepository userRepository) {

        this.bookingRepository = bookingRepository;
        this.facilityRepository = facilityRepository;
        this.userRepository = userRepository;
    }

    // Create booking
    public BookingResponse createBooking(
            BookingRequest request,
            String userEmail) {

        // Validate time
        if (!request.getStartTime().isBefore(request.getEndTime())) {
            throw new RuntimeException(
                    "Start time must be before end time"
            );
        }

        // Find facility
        Facility facility = facilityRepository
                .findById(request.getFacilityId())
                .orElseThrow(() ->
                        new RuntimeException("Facility not found"));

        // Find logged-in user
        User user = userRepository
                .findByEmail(userEmail)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        // Check overlapping bookings
        List<Booking> existingBookings =
        bookingRepository
                .findByFacilityIdAndBookingDate(
                        request.getFacilityId(),
                        request.getBookingDate()
                );

        for (Booking existing : existingBookings) {

            if (existing.getStatus() == BookingStatus.CANCELLED) {
                continue;
            }

            if (timesOverlap(
                    existing.getStartTime(),
                    existing.getEndTime(),
                    request.getStartTime(),
                    request.getEndTime())) {

                throw new RuntimeException(
                        "Facility is already booked for the selected time"
                );
            }
        }

        // Create booking
        Booking booking = new Booking(
                facility,
                user,
                request.getBookingDate(),
                request.getStartTime(),
                request.getEndTime(),
                request.getPurpose(),
                BookingStatus.PENDING
        );

        Booking savedBooking = bookingRepository.save(booking);

        return mapToResponse(savedBooking);
    }

    // View user's own bookings
    public List<BookingResponse> getMyBookings(String userEmail) {

        User user = userRepository
                .findByEmail(userEmail)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        return bookingRepository
                .findByBookedById(user.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // Cancel user's own booking
    public BookingResponse cancelBooking(
            UUID bookingId,
            String userEmail) {

        Booking booking = bookingRepository
                .findById(bookingId)
                .orElseThrow(() ->
                        new RuntimeException("Booking not found"));

        User user = userRepository
                .findByEmail(userEmail)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        // Ownership check
        if (!booking.getBookedBy().getId().equals(user.getId())) {
            throw new RuntimeException(
                    "You cannot modify another user's booking"
            );
        }

        booking.setStatus(BookingStatus.CANCELLED);

        Booking updatedBooking =
                bookingRepository.save(booking);

        return mapToResponse(updatedBooking);
    }

    // Check time overlap
    private boolean timesOverlap(
            LocalTime existingStart,
            LocalTime existingEnd,
            LocalTime newStart,
            LocalTime newEnd) {

        return existingStart.isBefore(newEnd)
                && existingEnd.isAfter(newStart);
    }

    // Convert Entity → Response
    private BookingResponse mapToResponse(Booking booking) {

        return new BookingResponse(
                booking.getId(),
                booking.getFacility().getId(),
                booking.getFacility().getName(),
                booking.getBookedBy().getId(),
                booking.getBookingDate(),
                booking.getStartTime(),
                booking.getEndTime(),
                booking.getPurpose(),
                booking.getStatus()
        );
    }

    public List<BookingResponse> getAllBookings() {

    return bookingRepository.findAll()
            .stream()
            .map(this::mapToResponse)
            .toList();
}

    public List<BookingResponse> getAvailability(
        UUID facilityId,
        LocalDate date) {

    // Make sure the facility exists
    facilityRepository.findById(facilityId)
            .orElseThrow(() ->
                    new RuntimeException("Facility not found"));

    return bookingRepository
            .findByFacilityIdAndBookingDate(facilityId, date)
            .stream()
            .map(this::mapToResponse)
            .toList();
}
}