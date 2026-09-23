package com.campusconnect.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class AssessmentPerformanceResponse {

    private UUID assessmentId;

    private String assessmentTitle;

    private String assessmentType;

    private LocalDate assessmentDate;

    private BigDecimal marksObtained;

    private BigDecimal maximumMarks;

    private BigDecimal percentage;
}