package com.example.kafkaeventdrivenapp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "cases")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "case_id")
    private Long caseId;
    
    // Foreign Key to Person
    @NotNull(message = "Person ID is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "person_id", nullable = false, foreignKey = @ForeignKey(name = "fk_case_person"))
    private PersonEntity person;
    
    // Case Numbers
    @NotBlank(message = "CMIPS case number is required")
    @Pattern(regexp = "^\\d{7}$", message = "CMIPS case number must be 7 digits")
    @Column(name = "cmips_case_number", nullable = false, length = 7, unique = true)
    private String cmipsCaseNumber; // 7-digit CMIPS case number
    
    @Pattern(regexp = "^\\d{10}$", message = "Legacy case number must be 10 digits")
    @Column(name = "legacy_case_number", length = 10)
    private String legacyCaseNumber; // 10-digit legacy case number
    
    // Case Status
    @NotBlank(message = "Case status is required")
    @Column(name = "case_status", nullable = false, length = 50)
    private String caseStatus = "PENDING"; // ACTIVE, INACTIVE, CLOSED, PENDING
    
    // Location Information
    @NotBlank(message = "County code is required")
    @Column(name = "county_code", nullable = false, length = 10)
    private String countyCode;
    
    @Column(name = "district_id", length = 50)
    private String districtId;
    
    @Column(name = "district_name", length = 100)
    private String districtName;
    
    // Assignment
    @NotBlank(message = "Assigned caseworker ID is required")
    @Column(name = "assigned_caseworker_id", nullable = false, length = 100)
    private String assignedCaseworkerId;
    
    // Dates
    @Column(name = "case_opened_date")
    private LocalDate caseOpenedDate;
    
    @Column(name = "case_closed_date")
    private LocalDate caseClosedDate;
    
    // Additional Case Information
    @Column(name = "case_notes", columnDefinition = "TEXT")
    private String caseNotes;
    
    // Audit Fields
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "created_by", length = 100)
    private String createdBy;
    
    // Helper methods
    public boolean isActive() {
        return "ACTIVE".equals(caseStatus);
    }
    
    public boolean isClosed() {
        return "CLOSED".equals(caseStatus);
    }
    
    public boolean isPending() {
        return "PENDING".equals(caseStatus);
    }
    
    public void closeCase() {
        this.caseStatus = "CLOSED";
        this.caseClosedDate = LocalDate.now();
    }
    
    public void activateCase() {
        this.caseStatus = "ACTIVE";
        if (this.caseOpenedDate == null) {
            this.caseOpenedDate = LocalDate.now();
        }
    }
}

