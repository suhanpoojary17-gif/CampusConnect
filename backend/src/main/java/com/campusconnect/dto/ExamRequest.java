package com.campusconnect.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Getter
@Setter
public class ExamRequest {

    @NotBlank(message = "Exam title is required")
    @Size(max = 200, message = "Exam title must not exceed 200 characters")
    private String title;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @NotNull(message = "Exam date is required")
    private LocalDate examDate;

    @NotNull(message = "Start time is required")
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    private LocalTime endTime;

    @NotNull(message = "Maximum marks are required")
    @DecimalMin(
            value = "0.01",
            message = "Maximum marks must be greater than 0"
    )
    private BigDecimal maximumMarks;

    @NotNull(message = "Section ID is required")
    private UUID sectionId;

    @NotNull(message = "Subject ID is required")
    private UUID subjectId;
}