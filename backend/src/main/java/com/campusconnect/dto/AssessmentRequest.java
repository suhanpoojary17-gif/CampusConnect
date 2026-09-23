package com.campusconnect.dto;

import com.campusconnect.model.AssessmentType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class AssessmentRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @NotNull(message = "Assessment type is required")
    private AssessmentType type;

    @NotNull(message = "Maximum marks are required")
    @DecimalMin(value = "0.01", message = "Maximum marks must be greater than 0")
    private BigDecimal maximumMarks;

    @NotNull(message = "Assessment date is required")
    private LocalDate assessmentDate;

    @NotNull(message = "Section ID is required")
    private UUID sectionId;

    @NotNull(message = "Subject ID is required")
    private UUID subjectId;
}