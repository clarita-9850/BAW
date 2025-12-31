package com.cmips.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Case Contact Entity - Tracks contacts associated with cases
 * Based on DSD Section 21 - BR SE 44, 45
 */
@Entity
@Table(name = "case_contacts")
public class CaseContactEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "case_id", nullable = false)
    private Long caseId;

    @Column(name = "recipient_id")
    private Long recipientId;

    @Column(name = "contact_type", length = 50)
    private String contactType;

    @Column(name = "contact_name", length = 200)
    private String contactName;

    @Column(name = "first_name", length = 100)
    private String firstName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "email", length = 200)
    private String email;

    @Column(name = "relationship", length = 100)
    private String relationship;

    @Column(name = "street_address", length = 300)
    private String streetAddress;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "state", length = 2)
    private String state;

    @Column(name = "zip_code", length = 10)
    private String zipCode;

    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "is_designee")
    private Boolean isDesignee;

    @Column(name = "synced_to_payroll")
    private Boolean syncedToPayroll;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    public CaseContactEntity() {}

    private CaseContactEntity(Builder builder) {
        this.id = builder.id;
        this.caseId = builder.caseId;
        this.recipientId = builder.recipientId;
        this.contactType = builder.contactType;
        this.contactName = builder.contactName;
        this.firstName = builder.firstName;
        this.lastName = builder.lastName;
        this.phone = builder.phone;
        this.email = builder.email;
        this.relationship = builder.relationship;
        this.streetAddress = builder.streetAddress;
        this.city = builder.city;
        this.state = builder.state;
        this.zipCode = builder.zipCode;
        this.status = builder.status;
        this.startDate = builder.startDate;
        this.endDate = builder.endDate;
        this.isDesignee = builder.isDesignee;
        this.syncedToPayroll = builder.syncedToPayroll;
        this.notes = builder.notes;
        this.createdAt = builder.createdAt;
        this.createdBy = builder.createdBy;
        this.updatedAt = builder.updatedAt;
        this.updatedBy = builder.updatedBy;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (startDate == null) startDate = LocalDate.now();
        if (endDate == null) endDate = LocalDate.of(9999, 12, 31);
        if (status == null) status = "ACTIVE";
        if (contactName != null) contactName = contactName.toUpperCase();
        if (firstName != null) firstName = firstName.toUpperCase();
        if (lastName != null) lastName = lastName.toUpperCase();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        if (contactName != null) contactName = contactName.toUpperCase();
        if (firstName != null) firstName = firstName.toUpperCase();
        if (lastName != null) lastName = lastName.toUpperCase();
    }

    public void inactivate() {
        this.endDate = LocalDate.now();
        this.status = "INACTIVE";
    }

    public boolean isActive() {
        return "ACTIVE".equals(status);
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCaseId() { return caseId; }
    public void setCaseId(Long caseId) { this.caseId = caseId; }

    public Long getRecipientId() { return recipientId; }
    public void setRecipientId(Long recipientId) { this.recipientId = recipientId; }

    public String getContactType() { return contactType; }
    public void setContactType(String contactType) { this.contactType = contactType; }

    public String getContactName() { return contactName; }
    public void setContactName(String contactName) { this.contactName = contactName; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRelationship() { return relationship; }
    public void setRelationship(String relationship) { this.relationship = relationship; }

    public String getStreetAddress() { return streetAddress; }
    public void setStreetAddress(String streetAddress) { this.streetAddress = streetAddress; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getZipCode() { return zipCode; }
    public void setZipCode(String zipCode) { this.zipCode = zipCode; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public Boolean getIsDesignee() { return isDesignee; }
    public void setIsDesignee(Boolean isDesignee) { this.isDesignee = isDesignee; }

    public Boolean getSyncedToPayroll() { return syncedToPayroll; }
    public void setSyncedToPayroll(Boolean syncedToPayroll) { this.syncedToPayroll = syncedToPayroll; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private Long caseId;
        private Long recipientId;
        private String contactType;
        private String contactName;
        private String firstName;
        private String lastName;
        private String phone;
        private String email;
        private String relationship;
        private String streetAddress;
        private String city;
        private String state;
        private String zipCode;
        private String status;
        private LocalDate startDate;
        private LocalDate endDate;
        private Boolean isDesignee;
        private Boolean syncedToPayroll;
        private String notes;
        private LocalDateTime createdAt;
        private String createdBy;
        private LocalDateTime updatedAt;
        private String updatedBy;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder caseId(Long caseId) { this.caseId = caseId; return this; }
        public Builder recipientId(Long recipientId) { this.recipientId = recipientId; return this; }
        public Builder contactType(String contactType) { this.contactType = contactType; return this; }
        public Builder contactName(String contactName) { this.contactName = contactName; return this; }
        public Builder firstName(String firstName) { this.firstName = firstName; return this; }
        public Builder lastName(String lastName) { this.lastName = lastName; return this; }
        public Builder phone(String phone) { this.phone = phone; return this; }
        public Builder email(String email) { this.email = email; return this; }
        public Builder relationship(String relationship) { this.relationship = relationship; return this; }
        public Builder streetAddress(String streetAddress) { this.streetAddress = streetAddress; return this; }
        public Builder city(String city) { this.city = city; return this; }
        public Builder state(String state) { this.state = state; return this; }
        public Builder zipCode(String zipCode) { this.zipCode = zipCode; return this; }
        public Builder status(String status) { this.status = status; return this; }
        public Builder startDate(LocalDate startDate) { this.startDate = startDate; return this; }
        public Builder endDate(LocalDate endDate) { this.endDate = endDate; return this; }
        public Builder isDesignee(Boolean isDesignee) { this.isDesignee = isDesignee; return this; }
        public Builder syncedToPayroll(Boolean syncedToPayroll) { this.syncedToPayroll = syncedToPayroll; return this; }
        public Builder notes(String notes) { this.notes = notes; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder createdBy(String createdBy) { this.createdBy = createdBy; return this; }
        public Builder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }
        public Builder updatedBy(String updatedBy) { this.updatedBy = updatedBy; return this; }

        public CaseContactEntity build() { return new CaseContactEntity(this); }
    }
}
