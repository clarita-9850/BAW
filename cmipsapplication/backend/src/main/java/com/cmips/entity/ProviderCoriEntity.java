package com.cmips.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Provider CORI (Criminal Offender Record Information) Entity
 * Based on DSD Section 23 - BR PVM 31-39
 */
@Entity
@Table(name = "provider_cori")
public class ProviderCoriEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "provider_id", nullable = false)
    private Long providerId;

    @Column(name = "conviction_date")
    private LocalDate convictionDate;

    @Column(name = "tier", length = 10)
    private String tier;

    @Column(name = "crime_description", length = 500)
    private String crimeDescription;

    @Column(name = "crime_code", length = 50)
    private String crimeCode;

    @Column(name = "cori_end_date")
    private LocalDate coriEndDate;

    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "general_exception_granted")
    private Boolean generalExceptionGranted;

    @Column(name = "general_exception_begin_date")
    private LocalDate generalExceptionBeginDate;

    @Column(name = "general_exception_end_date")
    private LocalDate generalExceptionEndDate;

    @Column(name = "general_exception_notes", columnDefinition = "TEXT")
    private String generalExceptionNotes;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    public ProviderCoriEntity() {}

    private ProviderCoriEntity(Builder builder) {
        this.id = builder.id;
        this.providerId = builder.providerId;
        this.convictionDate = builder.convictionDate;
        this.tier = builder.tier;
        this.crimeDescription = builder.crimeDescription;
        this.crimeCode = builder.crimeCode;
        this.coriEndDate = builder.coriEndDate;
        this.status = builder.status;
        this.generalExceptionGranted = builder.generalExceptionGranted;
        this.generalExceptionBeginDate = builder.generalExceptionBeginDate;
        this.generalExceptionEndDate = builder.generalExceptionEndDate;
        this.generalExceptionNotes = builder.generalExceptionNotes;
        this.createdAt = builder.createdAt;
        this.createdBy = builder.createdBy;
        this.updatedAt = builder.updatedAt;
        this.updatedBy = builder.updatedBy;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) status = "ACTIVE";
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public boolean isTier1() { return "TIER_1".equals(tier); }
    public boolean isTier2() { return "TIER_2".equals(tier); }

    public boolean hasActiveGeneralException() {
        if (!Boolean.TRUE.equals(generalExceptionGranted)) return false;
        if (generalExceptionBeginDate == null) return false;
        LocalDate today = LocalDate.now();
        if (generalExceptionBeginDate.isAfter(today)) return false;
        if (generalExceptionEndDate != null && generalExceptionEndDate.isBefore(today)) return false;
        return true;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getProviderId() { return providerId; }
    public void setProviderId(Long providerId) { this.providerId = providerId; }

    public LocalDate getConvictionDate() { return convictionDate; }
    public void setConvictionDate(LocalDate convictionDate) { this.convictionDate = convictionDate; }

    public String getTier() { return tier; }
    public void setTier(String tier) { this.tier = tier; }

    public String getCrimeDescription() { return crimeDescription; }
    public void setCrimeDescription(String crimeDescription) { this.crimeDescription = crimeDescription; }

    public String getCrimeCode() { return crimeCode; }
    public void setCrimeCode(String crimeCode) { this.crimeCode = crimeCode; }

    public LocalDate getCoriEndDate() { return coriEndDate; }
    public void setCoriEndDate(LocalDate coriEndDate) { this.coriEndDate = coriEndDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Boolean getGeneralExceptionGranted() { return generalExceptionGranted; }
    public void setGeneralExceptionGranted(Boolean generalExceptionGranted) { this.generalExceptionGranted = generalExceptionGranted; }

    public LocalDate getGeneralExceptionBeginDate() { return generalExceptionBeginDate; }
    public void setGeneralExceptionBeginDate(LocalDate generalExceptionBeginDate) { this.generalExceptionBeginDate = generalExceptionBeginDate; }

    public LocalDate getGeneralExceptionEndDate() { return generalExceptionEndDate; }
    public void setGeneralExceptionEndDate(LocalDate generalExceptionEndDate) { this.generalExceptionEndDate = generalExceptionEndDate; }

    public String getGeneralExceptionNotes() { return generalExceptionNotes; }
    public void setGeneralExceptionNotes(String generalExceptionNotes) { this.generalExceptionNotes = generalExceptionNotes; }

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
        private Long providerId;
        private LocalDate convictionDate;
        private String tier;
        private String crimeDescription;
        private String crimeCode;
        private LocalDate coriEndDate;
        private String status;
        private Boolean generalExceptionGranted;
        private LocalDate generalExceptionBeginDate;
        private LocalDate generalExceptionEndDate;
        private String generalExceptionNotes;
        private LocalDateTime createdAt;
        private String createdBy;
        private LocalDateTime updatedAt;
        private String updatedBy;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder providerId(Long providerId) { this.providerId = providerId; return this; }
        public Builder convictionDate(LocalDate convictionDate) { this.convictionDate = convictionDate; return this; }
        public Builder tier(String tier) { this.tier = tier; return this; }
        public Builder crimeDescription(String crimeDescription) { this.crimeDescription = crimeDescription; return this; }
        public Builder crimeCode(String crimeCode) { this.crimeCode = crimeCode; return this; }
        public Builder coriEndDate(LocalDate coriEndDate) { this.coriEndDate = coriEndDate; return this; }
        public Builder status(String status) { this.status = status; return this; }
        public Builder generalExceptionGranted(Boolean generalExceptionGranted) { this.generalExceptionGranted = generalExceptionGranted; return this; }
        public Builder generalExceptionBeginDate(LocalDate generalExceptionBeginDate) { this.generalExceptionBeginDate = generalExceptionBeginDate; return this; }
        public Builder generalExceptionEndDate(LocalDate generalExceptionEndDate) { this.generalExceptionEndDate = generalExceptionEndDate; return this; }
        public Builder generalExceptionNotes(String generalExceptionNotes) { this.generalExceptionNotes = generalExceptionNotes; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder createdBy(String createdBy) { this.createdBy = createdBy; return this; }
        public Builder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }
        public Builder updatedBy(String updatedBy) { this.updatedBy = updatedBy; return this; }

        public ProviderCoriEntity build() { return new ProviderCoriEntity(this); }
    }
}
