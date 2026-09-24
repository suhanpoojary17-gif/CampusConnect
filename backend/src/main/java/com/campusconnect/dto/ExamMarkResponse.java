package com.campusconnect.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class ExamMarkResponse {

    private UUID id;

    private UUID examId;

    private String examTitle;

    private BigDecimal maximumMarks;

    private UUID studentId;

    private String studentName;

    private BigDecimal marks;

    private BigDecimal percentage;

    private LocalDateTime gradedAt;
}