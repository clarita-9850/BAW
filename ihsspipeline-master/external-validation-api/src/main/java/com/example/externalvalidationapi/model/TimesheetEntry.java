package com.example.externalvalidationapi.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;

public class TimesheetEntry {
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;
    private double hours;
    private String description;
    private String projectCode;

    // Constructors
    public TimesheetEntry() {}

    public TimesheetEntry(LocalDate date, double hours, String description, String projectCode) {
        this.date = date;
        this.hours = hours;
        this.description = description;
        this.projectCode = projectCode;
    }

    // Getters and Setters
    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public double getHours() {
        return hours;
    }

    public void setHours(double hours) {
        this.hours = hours;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getProjectCode() {
        return projectCode;
    }

    public void setProjectCode(String projectCode) {
        this.projectCode = projectCode;
    }

    @Override
    public String toString() {
        return "TimesheetEntry{" +
                "date=" + date +
                ", hours=" + hours +
                ", description='" + description + '\'' +
                ", projectCode='" + projectCode + '\'' +
                '}';
    }
}
