package com.campusconnect.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class StudentMarkResponse {

    private UUID id;

    private UUID assessmentId;

    private String assessmentTitle;

    private BigDecimal maximumMarks;

    private UUID studentId;

    private String studentName;

    private BigDecimal marks;

    private LocalDateTime gradedAt;
}