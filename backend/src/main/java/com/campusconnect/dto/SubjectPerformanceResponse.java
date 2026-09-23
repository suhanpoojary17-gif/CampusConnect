package com.campusconnect.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class SubjectPerformanceResponse {

    private UUID subjectId;
    private String subjectName;
    private String subjectCode;

    private BigDecimal totalMarksObtained;
    private BigDecimal totalMaximumMarks;

    private BigDecimal percentage;
    private String performanceLevel;

    private List<AssessmentPerformanceResponse> assessments;
}