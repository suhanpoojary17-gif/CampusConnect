package com.campusconnect.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class BulkExamRequest {

    @NotEmpty(message = "Exam list cannot be empty")
    @Valid
    private List<ExamRequest> exams;
}