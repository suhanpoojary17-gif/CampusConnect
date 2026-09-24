package com.campusconnect.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class StudentExamResultResponse {

    private UUID examId;

    private String examTitle;

    private LocalDate examDate;

    private UUID subjectId;

    private String subjectName;

    private String subjectCode;

    private BigDecimal maximumMarks;

    private BigDecimal marksObtained;

    private BigDecimal percentage;

    private LocalDateTime gradedAt;
}