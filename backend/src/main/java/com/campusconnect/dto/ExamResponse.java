package com.campusconnect.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Getter
@Setter
public class ExamResponse {

    private UUID id;

    private String title;

    private String description;

    private LocalDate examDate;

    private LocalTime startTime;

    private LocalTime endTime;

    private BigDecimal maximumMarks;

    private UUID sectionId;

    private String sectionName;

    private UUID subjectId;

    private String subjectName;

    private String subjectCode;
}