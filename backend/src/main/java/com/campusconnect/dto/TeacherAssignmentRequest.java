package com.campusconnect.dto;

import java.util.UUID;

public class TeacherAssignmentRequest {

    private Long teacherId;
    private UUID sectionId;
    private UUID subjectId;

    public TeacherAssignmentRequest() {
    }

    public Long getTeacherId() {
        return teacherId;
    }

    public UUID getSectionId() {
        return sectionId;
    }

    public UUID getSubjectId() {
        return subjectId;
    }

    public void setTeacherId(Long teacherId) {
        this.teacherId = teacherId;
    }

    public void setSectionId(UUID sectionId) {
        this.sectionId = sectionId;
    }

    public void setSubjectId(UUID subjectId) {
        this.subjectId = subjectId;
    }
}