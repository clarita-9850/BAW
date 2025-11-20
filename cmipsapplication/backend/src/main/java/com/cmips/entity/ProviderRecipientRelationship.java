package com.cmips.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "provider_recipient_relationships")
public class ProviderRecipientRelationship {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String providerId;
    
    @Column(nullable = false)
    private String providerName;
    
    @Column(nullable = false)
    private String recipientId;
    
    @Column(nullable = false)
    private String recipientName;
    
    @Column(nullable = false)
    private String caseNumber;
    
    @Column(nullable = false)
    private Integer authorizedHoursPerMonth;
    
    @Column(nullable = false)
    private String status; // ACTIVE, INACTIVE, PENDING
    
    @Column
    private String relationship; // PRIMARY, BACKUP, RESPITE
    
    @Column
    private LocalDate startDate;
    
    @Column
    private LocalDate endDate;
    
    @Column
    private String county;
    
    @Column
    private String district;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getProviderId() { return providerId; }
    public void setProviderId(String providerId) { this.providerId = providerId; }
    
    public String getProviderName() { return providerName; }
    public void setProviderName(String providerName) { this.providerName = providerName; }
    
    public String getRecipientId() { return recipientId; }
    public void setRecipientId(String recipientId) { this.recipientId = recipientId; }
    
    public String getRecipientName() { return recipientName; }
    public void setRecipientName(String recipientName) { this.recipientName = recipientName; }
    
    public String getCaseNumber() { return caseNumber; }
    public void setCaseNumber(String caseNumber) { this.caseNumber = caseNumber; }
    
    public Integer getAuthorizedHoursPerMonth() { return authorizedHoursPerMonth; }
    public void setAuthorizedHoursPerMonth(Integer authorizedHoursPerMonth) { this.authorizedHoursPerMonth = authorizedHoursPerMonth; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getRelationship() { return relationship; }
    public void setRelationship(String relationship) { this.relationship = relationship; }
    
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    
    public String getCounty() { return county; }
    public void setCounty(String county) { this.county = county; }
    
    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}


