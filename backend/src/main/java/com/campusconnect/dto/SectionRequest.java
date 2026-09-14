package com.campusconnect.dto;

import java.util.UUID;

public class SectionRequest {

    private String name;
    private UUID departmentId;
    private UUID yearId;
    private UUID semesterId;

    public SectionRequest() {
    }

    public String getName() {
        return name;
    }

    public UUID getDepartmentId() {
        return departmentId;
    }

    public UUID getYearId() {
        return yearId;
    }

    public UUID getSemesterId() {
        return semesterId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDepartmentId(UUID departmentId) {
        this.departmentId = departmentId;
    }

    public void setYearId(UUID yearId) {
        this.yearId = yearId;
    }

    public void setSemesterId(UUID semesterId) {
        this.semesterId = semesterId;
    }
}