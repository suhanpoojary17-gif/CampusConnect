package com.campusconnect.dto;

import java.util.UUID;

public class FacilityResponse {

    private UUID id;
    private String name;
    private String type;
    private Integer capacity;
    private String location;

    public FacilityResponse() {
    }

    public FacilityResponse(UUID id, String name, String type,
                            Integer capacity, String location) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.capacity = capacity;
        this.location = location;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public String getLocation() {
        return location;
    }
}