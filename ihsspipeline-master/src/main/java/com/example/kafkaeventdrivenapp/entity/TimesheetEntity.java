package com.example.kafkaeventdrivenapp.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "timesheets")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimesheetEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @Column(name = "employee_id", nullable = false)
    private String employeeId;
    
    @Column(name = "employee_name", nullable = false)
    private String employeeName;
    
    @Column(name = "user_id", nullable = false)
    private String userId;
    
    @Column(name = "department", nullable = false)
    private String department;
    
    @Column(name = "location", nullable = false)
    private String location;
    
    @Column(name = "pay_period_start", nullable = false)
    private LocalDate payPeriodStart;
    
    @Column(name = "pay_period_end", nullable = false)
    private LocalDate payPeriodEnd;
    
    @Column(name = "regular_hours", precision = 10, scale = 2)
    private BigDecimal regularHours;
    
    @Column(name = "overtime_hours", precision = 10, scale = 2)
    private BigDecimal overtimeHours;
    
    @Column(name = "sick_hours", precision = 10, scale = 2)
    private BigDecimal sickHours;
    
    @Column(name = "vacation_hours", precision = 10, scale = 2)
    private BigDecimal vacationHours;
    
    @Column(name = "holiday_hours", precision = 10, scale = 2)
    private BigDecimal holidayHours;
    
    @Column(name = "total_hours", precision = 10, scale = 2)
    private BigDecimal totalHours;
    
    @Column(name = "status", nullable = false)
    private String status; // DRAFT, SUBMITTED, APPROVED, REJECTED, REVISION_REQUESTED, PROCESSED
    
    @Column(name = "comments", length = 1000)
    private String comments;
    
    @Column(name = "supervisor_comments", length = 1000)
    private String supervisorComments;
    
    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;
    
    @Column(name = "submitted_by")
    private String submittedBy;
    
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;
    
    @Column(name = "approved_by")
    private String approvedBy;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Provider Demographic Fields
    @Column(name = "provider_gender", length = 50)
    private String providerGender;
    
    @Column(name = "provider_ethnicity", length = 100)
    private String providerEthnicity;
    
    @Column(name = "provider_age_group", length = 50)
    private String providerAgeGroup;
    
    @Column(name = "provider_date_of_birth")
    private LocalDate providerDateOfBirth;
    
    // Recipient Demographic Fields
    @Column(name = "recipient_gender", length = 50)
    private String recipientGender;
    
    @Column(name = "recipient_ethnicity", length = 100)
    private String recipientEthnicity;
    
    @Column(name = "recipient_age_group", length = 50)
    private String recipientAgeGroup;
    
    @Column(name = "recipient_date_of_birth")
    private LocalDate recipientDateOfBirth;
}
