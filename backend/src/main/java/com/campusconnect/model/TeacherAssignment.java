package com.campusconnect.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "teacher_assignments")
public class TeacherAssignment {

    @Id
    private String id;

    private String teacherId;
    private String departmentId;
    private String yearId;
    private String semesterId;
    private String sectionId;
    private String subjectId;

    public TeacherAssignment() {
    }

    public TeacherAssignment(
            String teacherId,
            String departmentId,
            String yearId,
            String semesterId,
            String sectionId,
            String subjectId) {

        this.teacherId = teacherId;
        this.departmentId = departmentId;
        this.yearId = yearId;
        this.semesterId = semesterId;
        this.sectionId = sectionId;
        this.subjectId = subjectId;
    }

    public String getId() {
        return id;
    }

    public String getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(String teacherId) {
        this.teacherId = teacherId;
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

    public String getSectionId() {
        return sectionId;
    }

    public void setSectionId(String sectionId) {
        this.sectionId = sectionId;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }
}
