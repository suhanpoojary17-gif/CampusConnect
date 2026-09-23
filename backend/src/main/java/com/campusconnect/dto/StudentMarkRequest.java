package com.campusconnect.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class StudentMarkRequest {

    @NotNull(message = "Student ID is required")
    private UUID studentId;

    @NotNull(message = "Marks are required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Marks cannot be negative")
    private BigDecimal marks;
}