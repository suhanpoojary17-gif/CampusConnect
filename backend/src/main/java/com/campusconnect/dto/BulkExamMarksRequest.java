package com.campusconnect.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class BulkExamMarksRequest {

    @NotEmpty(message = "Marks list cannot be empty")
    @Valid
    private List<ExamMarkEntryRequest> marks;
}