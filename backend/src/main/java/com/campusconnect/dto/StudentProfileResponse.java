package com.campusconnect.dto;

import java.util.UUID;

public class StudentProfileResponse {

    private UUID studentId;
    private String name;
    private UUID sectionId;
    private String sectionName;

    public StudentProfileResponse(
            UUID studentId,
            String name,
            UUID sectionId,
            String sectionName
    ) {
        this.studentId = studentId;
        this.name = name;
        this.sectionId = sectionId;
        this.sectionName = sectionName;
    }

    public UUID getStudentId() {
        return studentId;
    }

    public String getName() {
        return name;
    }

    public UUID getSectionId() {
        return sectionId;
    }

    public String getSectionName() {
        return sectionName;
    }
}