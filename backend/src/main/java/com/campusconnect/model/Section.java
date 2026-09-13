package com.campusconnect.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "sections")
public class Section {

    @Id
    private String id;

    private String name;

    private String departmentId;
    private String yearId;
    private String semesterId;

    public Section() {
    }

    public Section(String name, String departmentId,
                   String yearId, String semesterId) {
        this.name = name;
        this.departmentId = departmentId;
        this.yearId = yearId;
        this.semesterId = semesterId;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(String departmentId) {
        this.departmentId = departmentId;
    }

    public String getYearId() {
        return yearId;
    }

    public void setYearId(String yearId) {
        this.yearId = yearId;
    }

    public String getSemesterId() {
        return semesterId;
    }

    public void setSemesterId(String semesterId) {
        this.semesterId = semesterId;
    }
}