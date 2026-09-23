package com.campusconnect.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class AcademicPerformanceResponse {

    private BigDecimal totalMarksObtained;
    private BigDecimal totalMaximumMarks;

    private BigDecimal overallPercentage;
    private String performanceLevel;

    private List<SubjectPerformanceResponse> subjects;
}