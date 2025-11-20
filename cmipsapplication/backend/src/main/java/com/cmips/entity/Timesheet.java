package com.cmips.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Entity
@Table(name = "timesheets")
public class Timesheet {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String userId;
    
    @Column(nullable = false)
    private String employeeId;
    
    @Column(nullable = false)
    private String employeeName;
    
    @Column(nullable = false)
    private String department;
    
    @Column(nullable = false)
    private String location;
    
    @Column(nullable = false)
    @NotNull(message = "Pay period start date is required")
    private LocalDate payPeriodStart;
    
    @Column(nullable = false)
    @NotNull(message = "Pay period end date is required")
    private LocalDate payPeriodEnd;
    
    @Column(nullable = false, precision = 10, scale = 2)
    @DecimalMin(value = "0.0", message = "Regular hours must be non-negative")
    private BigDecimal regularHours = BigDecimal.ZERO;
    
    @Column(precision = 10, scale = 2)
    @DecimalMin(value = "0.0", message = "Overtime hours must be non-negative")
    private BigDecimal overtimeHours = BigDecimal.ZERO;
    
    @Column(precision = 10, scale = 2)
    @DecimalMin(value = "0.0", message = "Holiday hours must be non-negative")
    private BigDecimal holidayHours = BigDecimal.ZERO;
    
    @Column(precision = 10, scale = 2)
    @DecimalMin(value = "0.0", message = "Sick hours must be non-negative")
    private BigDecimal sickHours = BigDecimal.ZERO;
    
    @Column(precision = 10, scale = 2)
    @DecimalMin(value = "0.0", message = "Vacation hours must be non-negative")
    private BigDecimal vacationHours = BigDecimal.ZERO;
    
    @Column(precision = 10, scale = 2)
    @DecimalMin(value = "0.0", message = "Total hours must be non-negative")
    private BigDecimal totalHours = BigDecimal.ZERO;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TimesheetStatus status = TimesheetStatus.DRAFT;
    
    @Column(length = 1000)
    private String comments;
    
    @Column(length = 1000)
    private String supervisorComments;
    
    @Column
    private String approvedBy;
    
    @Column
    private LocalDateTime approvedAt;
    
    @Column
    private String submittedBy;
    
    @Column
    private LocalDateTime submittedAt;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Default constructor
    public Timesheet() {}
    
    // Constructor for basic timesheet creation
    public Timesheet(String userId, String employeeId, String employeeName, 
                    String department, String location, LocalDate payPeriodStart, 
                    LocalDate payPeriodEnd) {
        this.userId = userId;
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.department = department;
        this.location = location;
        this.payPeriodStart = payPeriodStart;
        this.payPeriodEnd = payPeriodEnd;
    }
    
    // Calculate total hours
    public void calculateTotalHours() {
        this.totalHours = regularHours
            .add(overtimeHours != null ? overtimeHours : BigDecimal.ZERO)
            .add(holidayHours != null ? holidayHours : BigDecimal.ZERO)
            .add(sickHours != null ? sickHours : BigDecimal.ZERO)
            .add(vacationHours != null ? vacationHours : BigDecimal.ZERO);
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

