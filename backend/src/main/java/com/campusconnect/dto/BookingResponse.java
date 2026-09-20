package com.campusconnect.dto;

import com.campusconnect.entity.BookingStatus;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public class BookingResponse {

    private UUID id;
    private UUID facilityId;
    private String facilityName;
    private Long bookedBy;
    private LocalDate bookingDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String purpose;
    private BookingStatus status;

    public BookingResponse(
            UUID id,
            UUID facilityId,
            String facilityName,
            Long bookedBy,
            LocalDate bookingDate,
            LocalTime startTime,
            LocalTime endTime,
            String purpose,
            BookingStatus status) {

        this.id = id;
        this.facilityId = facilityId;
        this.facilityName = facilityName;
        this.bookedBy = bookedBy;
        this.bookingDate = bookingDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.purpose = purpose;
        this.status = status;
    }

    public UUID getId() {
        return id;
    }

    public UUID getFacilityId() {
        return facilityId;
    }

    public String getFacilityName() {
        return facilityName;
    }

    public Long getBookedBy() {
        return bookedBy;
    }

    public LocalDate getBookingDate() {
        return bookingDate;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public String getPurpose() {
        return purpose;
    }

    public BookingStatus getStatus() {
        return status;
    }
}