package com.campusconnect.dto;

public class AttendanceForecastResponse {

    private double currentPercentage;
    private long upcomingClasses;
    private double bestCasePercentage;
    private double worstCasePercentage;

    public AttendanceForecastResponse(
            double currentPercentage,
            long upcomingClasses,
            double bestCasePercentage,
            double worstCasePercentage) {

        this.currentPercentage = currentPercentage;
        this.upcomingClasses = upcomingClasses;
        this.bestCasePercentage = bestCasePercentage;
        this.worstCasePercentage = worstCasePercentage;
    }

    public double getCurrentPercentage() {
        return currentPercentage;
    }

    public long getUpcomingClasses() {
        return upcomingClasses;
    }

    public double getBestCasePercentage() {
        return bestCasePercentage;
    }

    public double getWorstCasePercentage() {
        return worstCasePercentage;
    }
}