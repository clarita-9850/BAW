package com.cmips.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.math.BigDecimal;

public class TimesheetCreateRequest {
    
    @NotBlank(message = "Employee ID is required")
    private String employeeId;
    
    @NotBlank(message = "Employee name is required")
    private String employeeName;
    
    @NotBlank(message = "Department is required")
    private String department;
    
    @NotBlank(message = "Location is required")
    private String location;
    
    @NotNull(message = "Pay period start date is required")
    private LocalDate payPeriodStart;
    
    @NotNull(message = "Pay period end date is required")
    private LocalDate payPeriodEnd;
    
    @DecimalMin(value = "0.0", message = "Regular hours must be non-negative")
    private BigDecimal regularHours = BigDecimal.ZERO;
    
    @DecimalMin(value = "0.0", message = "Overtime hours must be non-negative")
    private BigDecimal overtimeHours = BigDecimal.ZERO;
    
    @DecimalMin(value = "0.0", message = "Holiday hours must be non-negative")
    private BigDecimal holidayHours = BigDecimal.ZERO;
    
    @DecimalMin(value = "0.0", message = "Sick hours must be non-negative")
    private BigDecimal sickHours = BigDecimal.ZERO;
    
    @DecimalMin(value = "0.0", message = "Vacation hours must be non-negative")
    private BigDecimal vacationHours = BigDecimal.ZERO;
    
    private String comments;
    
    // Default constructor
    public TimesheetCreateRequest() {}
    
    // Getters and Setters
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
    
    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }
}


