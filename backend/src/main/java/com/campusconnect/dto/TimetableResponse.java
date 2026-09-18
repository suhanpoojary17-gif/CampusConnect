package com.campusconnect.dto;

import com.campusconnect.model.DayOfWeek;

import java.time.LocalTime;
import java.util.UUID;

public class TimetableResponse {

    private Long id;
    private UUID sectionId;
    private DayOfWeek day;
    private LocalTime startTime;
    private LocalTime endTime;
    private UUID subjectId;
    private Long teacherId;
    private String room;

    public TimetableResponse(
            Long id,
            UUID sectionId,
            DayOfWeek day,
            LocalTime startTime,
            LocalTime endTime,
            UUID subjectId,
            Long teacherId,
            String room
    ) {
        this.id = id;
        this.sectionId = sectionId;
        this.day = day;
        this.startTime = startTime;
        this.endTime = endTime;
        this.subjectId = subjectId;
        this.teacherId = teacherId;
        this.room = room;
    }

    public Long getId() {
        return id;
    }

    public UUID getSectionId() {
        return sectionId;
    }

    public DayOfWeek getDay() {
        return day;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public UUID getSubjectId() {
        return subjectId;
    }

    public Long getTeacherId() {
        return teacherId;
    }

    public String getRoom() {
        return room;
    }
}