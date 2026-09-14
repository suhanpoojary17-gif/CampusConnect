package com.campusconnect.dto;

import java.util.UUID;

public class SubjectRequest {

    private String name;
    private String code;
    private UUID sectionId;

    public SubjectRequest() {
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public UUID getSectionId() {
        return sectionId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setSectionId(UUID sectionId) {
        this.sectionId = sectionId;
    }
}