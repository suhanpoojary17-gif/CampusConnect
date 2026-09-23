package com.campusconnect.dto;

import com.campusconnect.model.AssessmentType;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class AssessmentResponse {

    private UUID id;

    private String title;

    private String description;

    private AssessmentType type;

    private BigDecimal maximumMarks;

    private LocalDate assessmentDate;

    private UUID sectionId;

    private String sectionName;

    private UUID subjectId;

    private String subjectName;

    private String subjectCode;

    private Long teacherId;

    private String teacherEmail;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}