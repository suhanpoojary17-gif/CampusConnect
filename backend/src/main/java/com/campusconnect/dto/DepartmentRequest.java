package com.campusconnect.dto;

public class DepartmentRequest {

    private String name;
    private String code;

    public DepartmentRequest() {
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCode(String code) {
        this.code = code;
    }
}