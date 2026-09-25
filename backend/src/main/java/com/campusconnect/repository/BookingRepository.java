package com.campusconnect.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.campusconnect.entity.Booking;
import com.campusconnect.entity.BookingStatus;

public interface BookingRepository extends JpaRepository<Booking, UUID> {

    List<Booking> findByBookedById(Long userId);

    List<Booking> findByFacilityIdAndBookingDate(
            UUID facilityId,
            LocalDate bookingDate
    );

    List<Booking> findByFacilityIdAndBookingDateAndStatus(
            UUID facilityId,
            LocalDate bookingDate,
            BookingStatus status
    );
}