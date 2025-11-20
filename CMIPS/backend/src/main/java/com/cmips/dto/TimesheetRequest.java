package com.cmips.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public class TimesheetRequest {
    
    @NotNull(message = "Date is required")
    private LocalDate date;
    
    @NotNull(message = "Hours is required")
    @Positive(message = "Hours must be positive")
    private Double hours;
    
    private String description;
    
    private String comments;
    
    // Default constructor
    public TimesheetRequest() {}
    
    // Constructor
    public TimesheetRequest(LocalDate date, Double hours, String description, String comments) {
        this.date = date;
        this.hours = hours;
        this.description = description;
        this.comments = comments;
    }
    
    // Getters and Setters
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    
    public Double getHours() { return hours; }
    public void setHours(Double hours) { this.hours = hours; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }
}
