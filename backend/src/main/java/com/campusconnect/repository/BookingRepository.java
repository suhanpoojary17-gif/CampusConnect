package com.campusconnect.repository;

import com.campusconnect.entity.Booking;
import com.campusconnect.entity.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

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