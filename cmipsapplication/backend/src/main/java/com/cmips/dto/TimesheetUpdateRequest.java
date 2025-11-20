package com.cmips.dto;

import com.cmips.entity.TimesheetStatus;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;

public class TimesheetUpdateRequest {
    
    @DecimalMin(value = "0.0", message = "Regular hours must be non-negative")
    private BigDecimal regularHours;
    
    @DecimalMin(value = "0.0", message = "Overtime hours must be non-negative")
    private BigDecimal overtimeHours;
    
    @DecimalMin(value = "0.0", message = "Holiday hours must be non-negative")
    private BigDecimal holidayHours;
    
    @DecimalMin(value = "0.0", message = "Sick hours must be non-negative")
    private BigDecimal sickHours;
    
    @DecimalMin(value = "0.0", message = "Vacation hours must be non-negative")
    private BigDecimal vacationHours;
    
    private String comments;
    
    private TimesheetStatus status;
    
    private String supervisorComments;
    
    // Default constructor
    public TimesheetUpdateRequest() {}
    
    // Getters and Setters
    public BigDecimal getRegularHours() { return regularHours; }
    public void setRegularHours(BigDecimal regularHours) { this.regularHours = regularHours; }
    
    public BigDecimal getOvertimeHours() { return overtimeHours; }
    public void setOvertimeHours(BigDecimal overtimeHours) { this.overtimeHours = overtimeHours; }
    
    public BigDecimal getHolidayHours() { return holidayHours; }
    public void setHolidayHours(BigDecimal holidayHours) { this.holidayHours = holidayHours; }
    
    public BigDecimal getSickHours() { return sickHours; }
    public void setSickHours(BigDecimal sickHours) { this.sickHours = sickHours; }
    
    public BigDecimal getVacationHours() { return vacationHours; }
    public void setVacationHours(BigDecimal vacationHours) { this.vacationHours = vacationHours; }
    
    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }
    
    public TimesheetStatus getStatus() { return status; }
    public void setStatus(TimesheetStatus status) { this.status = status; }
    
    public String getSupervisorComments() { return supervisorComments; }
    public void setSupervisorComments(String supervisorComments) { this.supervisorComments = supervisorComments; }
}


