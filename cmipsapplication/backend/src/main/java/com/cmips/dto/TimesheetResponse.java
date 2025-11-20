package com.cmips.dto;

import com.cmips.entity.TimesheetStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;

public class TimesheetResponse {
    
    private Long id;
    private String userId;
    private String employeeId;
    private String employeeName;
    private String department;
    private String location;
    private LocalDate payPeriodStart;
    private LocalDate payPeriodEnd;
    private BigDecimal regularHours;
    private BigDecimal overtimeHours;
    private BigDecimal holidayHours;
    private BigDecimal sickHours;
    private BigDecimal vacationHours;
    private BigDecimal totalHours;
    private TimesheetStatus status;
    private String comments;
    private String supervisorComments;
    private String approvedBy;
    private LocalDateTime approvedAt;
    private String submittedBy;
    private LocalDateTime submittedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Default constructor
    public TimesheetResponse() {}
    
    // Constructor from entity
    public TimesheetResponse(com.cmips.entity.Timesheet timesheet) {
        this.id = timesheet.getId();
        this.userId = timesheet.getUserId();
        this.employeeId = timesheet.getEmployeeId();
        this.employeeName = timesheet.getEmployeeName();
        this.department = timesheet.getDepartment();
        this.location = timesheet.getLocation();
        this.payPeriodStart = timesheet.getPayPeriodStart();
        this.payPeriodEnd = timesheet.getPayPeriodEnd();
        this.regularHours = timesheet.getRegularHours();
        this.overtimeHours = timesheet.getOvertimeHours();
        this.holidayHours = timesheet.getHolidayHours();
        this.sickHours = timesheet.getSickHours();
        this.vacationHours = timesheet.getVacationHours();
        this.totalHours = timesheet.getTotalHours();
        this.status = timesheet.getStatus();
        this.comments = timesheet.getComments();
        this.supervisorComments = timesheet.getSupervisorComments();
        this.approvedBy = timesheet.getApprovedBy();
        this.approvedAt = timesheet.getApprovedAt();
        this.submittedBy = timesheet.getSubmittedBy();
        this.submittedAt = timesheet.getSubmittedAt();
        this.createdAt = timesheet.getCreatedAt();
        this.updatedAt = timesheet.getUpdatedAt();
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }
    
    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
    
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    
    public LocalDate getPayPeriodStart() { return payPeriodStart; }
    public void setPayPeriodStart(LocalDate payPeriodStart) { this.payPeriodStart = payPeriodStart; }
    
    public LocalDate getPayPeriodEnd() { return payPeriodEnd; }
    public void setPayPeriodEnd(LocalDate payPeriodEnd) { this.payPeriodEnd = payPeriodEnd; }
    
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
    
    public BigDecimal getTotalHours() { return totalHours; }
    public void setTotalHours(BigDecimal totalHours) { this.totalHours = totalHours; }
    
    public TimesheetStatus getStatus() { return status; }
    public void setStatus(TimesheetStatus status) { this.status = status; }
    
    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }
    
    public String getSupervisorComments() { return supervisorComments; }
    public void setSupervisorComments(String supervisorComments) { this.supervisorComments = supervisorComments; }
    
    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }
    
    public LocalDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }
    
    public String getSubmittedBy() { return submittedBy; }
    public void setSubmittedBy(String submittedBy) { this.submittedBy = submittedBy; }
    
    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}


