package com.campusconnect.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class StudentPerformanceResponse {

    private UUID studentId;
    private String studentName;

    private BigDecimal totalMarksObtained;
    private BigDecimal totalMaximumMarks;

    private BigDecimal percentage;
    private String performanceLevel;
}