package com.cmips.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Referral Entity - Represents an initial contact/referral for IHSS services
 * Based on DSD Section 20 - Online Search, Initial Contact & Intake Application
 *
 * A referral is an initial inquiry that may or may not lead to an application.
 * Referrals are created when someone contacts the county about IHSS services.
 */
@Entity
@Table(name = "referrals")
public class ReferralEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    // Link to recipient/person record (if created)
    @Column(name = "recipient_id")
    private Long recipientId;

    // Referral Status
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ReferralStatus status = ReferralStatus.OPEN;

    // Referral Source - How person learned about IHSS
    @Enumerated(EnumType.STRING)
    @Column(name = "source")
    private ReferralSource source;

    @Column(name = "source_details", length = 500)
    private String sourceDetails; // Additional details about referral source

    // Contact Information (before person record is created)
    @Column(name = "contact_first_name", length = 100)
    private String contactFirstName;

    @Column(name = "contact_last_name", length = 100)
    private String contactLastName;

    @Column(name = "contact_phone", length = 20)
    private String contactPhone;

    @Column(name = "contact_email", length = 255)
    private String contactEmail;

    @Column(name = "contact_relationship", length = 100)
    private String contactRelationship; // Self, Family Member, Hospital, etc.

    // Potential Recipient Information
    @Column(name = "potential_recipient_name", length = 200)
    private String potentialRecipientName;

    @Column(name = "potential_recipient_dob")
    private LocalDate potentialRecipientDob;

    @Column(name = "potential_recipient_ssn", length = 11)
    private String potentialRecipientSsn;

    @Column(name = "potential_recipient_phone", length = 20)
    private String potentialRecipientPhone;

    // Address
    @Column(name = "street_address", length = 255)
    private String streetAddress;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "state", length = 2)
    private String state = "CA";

    @Column(name = "zip_code", length = 10)
    private String zipCode;

    // County Assignment
    @Column(name = "county_code", length = 10)
    private String countyCode;

    @Column(name = "county_name", length = 100)
    private String countyName;

    // Key Dates
    @Column(name = "referral_date", nullable = false)
    private LocalDate referralDate;

    @Column(name = "first_contact_date")
    private LocalDate firstContactDate;

    @Column(name = "follow_up_date")
    private LocalDate followUpDate;

    @Column(name = "closed_date")
    private LocalDate closedDate;

    // Closure Information
    @Enumerated(EnumType.STRING)
    @Column(name = "closed_reason")
    private ReferralClosedReason closedReason;

    @Column(name = "closed_reason_details", length = 500)
    private String closedReasonDetails;

    // Conversion to Application
    @Column(name = "converted_to_application")
    private Boolean convertedToApplication = false;

    @Column(name = "application_id")
    private String applicationId;

    @Column(name = "conversion_date")
    private LocalDate conversionDate;

    // Assignment
    @Column(name = "assigned_worker_id", length = 100)
    private String assignedWorkerId;

    @Column(name = "assigned_worker_name", length = 200)
    private String assignedWorkerName;

    // Initial Inquiry Details
    @Column(name = "inquiry_type", length = 100)
    private String inquiryType; // SERVICES, ELIGIBILITY, PROVIDER_INFO, OTHER

    @Column(name = "inquiry_notes", columnDefinition = "TEXT")
    private String inquiryNotes;

    // Priority
    @Enumerated(EnumType.STRING)
    @Column(name = "priority")
    private ReferralPriority priority = ReferralPriority.NORMAL;

    // Language Preference
    @Column(name = "preferred_language", length = 50)
    private String preferredLanguage;

    @Column(name = "interpreter_needed")
    private Boolean interpreterNeeded = false;

    // External Referral Information
    @Column(name = "referring_agency_name", length = 200)
    private String referringAgencyName;

    @Column(name = "referring_agency_contact", length = 200)
    private String referringAgencyContact;

    @Column(name = "referring_agency_phone", length = 20)
    private String referringAgencyPhone;

    @Column(name = "referring_agency_email", length = 255)
    private String referringAgencyEmail;

    @Column(name = "external_reference_number", length = 100)
    private String externalReferenceNumber;

    // Audit Fields
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    // Enums
    public enum ReferralStatus {
        OPEN,           // Initial status - referral is active
        PENDING,        // Waiting for follow-up or response
        IN_PROGRESS,    // Being processed
        CONVERTED,      // Converted to application
        CLOSED          // Closed without conversion
    }

    public enum ReferralSource {
        SELF,               // Self-referral
        FAMILY,             // Family member or friend
        HOSPITAL,           // Hospital discharge planner
        MEDICAL_PROVIDER,   // Doctor, nurse, clinic
        SOCIAL_SERVICES,    // Social worker, case manager
        COMMUNITY_ORG,      // Senior center, disability advocacy group
        OTHER_AGENCY,       // Another county or state agency
        ONLINE,             // Online inquiry or web form
        PHONE,              // Phone call to county
        WALK_IN,            // Walked into county office
        OTHER               // Other source
    }

    public enum ReferralClosedReason {
        NOT_INTERESTED,         // Person decided not to apply
        NOT_ELIGIBLE,           // Person not eligible for IHSS
        REFERRED_ELSEWHERE,     // Referred to different program
        UNABLE_TO_CONTACT,      // Could not reach person
        DUPLICATE,              // Duplicate referral
        CONVERTED_TO_APP,       // Converted to application
        MOVED_OUT_OF_COUNTY,    // Person moved
        DECEASED,               // Person passed away
        WITHDRAWN,              // Person withdrew interest
        OTHER                   // Other reason
    }

    public enum ReferralPriority {
        URGENT,     // Immediate attention needed
        HIGH,       // Priority processing
        NORMAL,     // Standard processing
        LOW         // Can wait
    }

    // Lifecycle hooks
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (referralDate == null) {
            referralDate = LocalDate.now();
        }
        if (status == null) {
            status = ReferralStatus.OPEN;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Constructors
    public ReferralEntity() {
    }

    // Builder pattern
    public static ReferralEntityBuilder builder() {
        return new ReferralEntityBuilder();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(Long recipientId) {
        this.recipientId = recipientId;
    }

    public ReferralStatus getStatus() {
        return status;
    }

    public void setStatus(ReferralStatus status) {
        this.status = status;
    }

    public ReferralSource getSource() {
        return source;
    }

    public void setSource(ReferralSource source) {
        this.source = source;
    }

    public String getSourceDetails() {
        return sourceDetails;
    }

    public void setSourceDetails(String sourceDetails) {
        this.sourceDetails = sourceDetails;
    }

    public String getContactFirstName() {
        return contactFirstName;
    }

    public void setContactFirstName(String contactFirstName) {
        this.contactFirstName = contactFirstName;
    }

    public String getContactLastName() {
        return contactLastName;
    }

    public void setContactLastName(String contactLastName) {
        this.contactLastName = contactLastName;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getContactRelationship() {
        return contactRelationship;
    }

    public void setContactRelationship(String contactRelationship) {
        this.contactRelationship = contactRelationship;
    }

    public String getPotentialRecipientName() {
        return potentialRecipientName;
    }

    public void setPotentialRecipientName(String potentialRecipientName) {
        this.potentialRecipientName = potentialRecipientName;
    }

    public LocalDate getPotentialRecipientDob() {
        return potentialRecipientDob;
    }

    public void setPotentialRecipientDob(LocalDate potentialRecipientDob) {
        this.potentialRecipientDob = potentialRecipientDob;
    }

    public String getPotentialRecipientSsn() {
        return potentialRecipientSsn;
    }

    public void setPotentialRecipientSsn(String potentialRecipientSsn) {
        this.potentialRecipientSsn = potentialRecipientSsn;
    }

    public String getPotentialRecipientPhone() {
        return potentialRecipientPhone;
    }

    public void setPotentialRecipientPhone(String potentialRecipientPhone) {
        this.potentialRecipientPhone = potentialRecipientPhone;
    }

    public String getStreetAddress() {
        return streetAddress;
    }

    public void setStreetAddress(String streetAddress) {
        this.streetAddress = streetAddress;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public String getCountyCode() {
        return countyCode;
    }

    public void setCountyCode(String countyCode) {
        this.countyCode = countyCode;
    }

    public String getCountyName() {
        return countyName;
    }

    public void setCountyName(String countyName) {
        this.countyName = countyName;
    }

    public LocalDate getReferralDate() {
        return referralDate;
    }

    public void setReferralDate(LocalDate referralDate) {
        this.referralDate = referralDate;
    }

    public LocalDate getFirstContactDate() {
        return firstContactDate;
    }

    public void setFirstContactDate(LocalDate firstContactDate) {
        this.firstContactDate = firstContactDate;
    }

    public LocalDate getFollowUpDate() {
        return followUpDate;
    }

    public void setFollowUpDate(LocalDate followUpDate) {
        this.followUpDate = followUpDate;
    }

    public LocalDate getClosedDate() {
        return closedDate;
    }

    public void setClosedDate(LocalDate closedDate) {
        this.closedDate = closedDate;
    }

    public ReferralClosedReason getClosedReason() {
        return closedReason;
    }

    public void setClosedReason(ReferralClosedReason closedReason) {
        this.closedReason = closedReason;
    }

    public String getClosedReasonDetails() {
        return closedReasonDetails;
    }

    public void setClosedReasonDetails(String closedReasonDetails) {
        this.closedReasonDetails = closedReasonDetails;
    }

    public Boolean getConvertedToApplication() {
        return convertedToApplication;
    }

    public void setConvertedToApplication(Boolean convertedToApplication) {
        this.convertedToApplication = convertedToApplication;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public LocalDate getConversionDate() {
        return conversionDate;
    }

    public void setConversionDate(LocalDate conversionDate) {
        this.conversionDate = conversionDate;
    }

    public String getAssignedWorkerId() {
        return assignedWorkerId;
    }

    public void setAssignedWorkerId(String assignedWorkerId) {
        this.assignedWorkerId = assignedWorkerId;
    }

    public String getAssignedWorkerName() {
        return assignedWorkerName;
    }

    public void setAssignedWorkerName(String assignedWorkerName) {
        this.assignedWorkerName = assignedWorkerName;
    }

    public String getInquiryType() {
        return inquiryType;
    }

    public void setInquiryType(String inquiryType) {
        this.inquiryType = inquiryType;
    }

    public String getInquiryNotes() {
        return inquiryNotes;
    }

    public void setInquiryNotes(String inquiryNotes) {
        this.inquiryNotes = inquiryNotes;
    }

    public ReferralPriority getPriority() {
        return priority;
    }

    public void setPriority(ReferralPriority priority) {
        this.priority = priority;
    }

    public String getPreferredLanguage() {
        return preferredLanguage;
    }

    public void setPreferredLanguage(String preferredLanguage) {
        this.preferredLanguage = preferredLanguage;
    }

    public Boolean getInterpreterNeeded() {
        return interpreterNeeded;
    }

    public void setInterpreterNeeded(Boolean interpreterNeeded) {
        this.interpreterNeeded = interpreterNeeded;
    }

    public String getReferringAgencyName() {
        return referringAgencyName;
    }

    public void setReferringAgencyName(String referringAgencyName) {
        this.referringAgencyName = referringAgencyName;
    }

    public String getReferringAgencyContact() {
        return referringAgencyContact;
    }

    public void setReferringAgencyContact(String referringAgencyContact) {
        this.referringAgencyContact = referringAgencyContact;
    }

    public String getReferringAgencyPhone() {
        return referringAgencyPhone;
    }

    public void setReferringAgencyPhone(String referringAgencyPhone) {
        this.referringAgencyPhone = referringAgencyPhone;
    }

    public String getReferringAgencyEmail() {
        return referringAgencyEmail;
    }

    public void setReferringAgencyEmail(String referringAgencyEmail) {
        this.referringAgencyEmail = referringAgencyEmail;
    }

    public String getExternalReferenceNumber() {
        return externalReferenceNumber;
    }

    public void setExternalReferenceNumber(String externalReferenceNumber) {
        this.externalReferenceNumber = externalReferenceNumber;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    // Builder class
    public static class ReferralEntityBuilder {
        private final ReferralEntity entity = new ReferralEntity();

        public ReferralEntityBuilder recipientId(Long recipientId) {
            entity.setRecipientId(recipientId);
            return this;
        }

        public ReferralEntityBuilder status(ReferralStatus status) {
            entity.setStatus(status);
            return this;
        }

        public ReferralEntityBuilder source(ReferralSource source) {
            entity.setSource(source);
            return this;
        }

        public ReferralEntityBuilder contactFirstName(String contactFirstName) {
            entity.setContactFirstName(contactFirstName);
            return this;
        }

        public ReferralEntityBuilder contactLastName(String contactLastName) {
            entity.setContactLastName(contactLastName);
            return this;
        }

        public ReferralEntityBuilder contactPhone(String contactPhone) {
            entity.setContactPhone(contactPhone);
            return this;
        }

        public ReferralEntityBuilder contactEmail(String contactEmail) {
            entity.setContactEmail(contactEmail);
            return this;
        }

        public ReferralEntityBuilder potentialRecipientName(String name) {
            entity.setPotentialRecipientName(name);
            return this;
        }

        public ReferralEntityBuilder countyCode(String countyCode) {
            entity.setCountyCode(countyCode);
            return this;
        }

        public ReferralEntityBuilder referralDate(LocalDate date) {
            entity.setReferralDate(date);
            return this;
        }

        public ReferralEntityBuilder assignedWorkerId(String workerId) {
            entity.setAssignedWorkerId(workerId);
            return this;
        }

        public ReferralEntityBuilder priority(ReferralPriority priority) {
            entity.setPriority(priority);
            return this;
        }

        public ReferralEntityBuilder createdBy(String createdBy) {
            entity.setCreatedBy(createdBy);
            return this;
        }

        public ReferralEntityBuilder potentialRecipientPhone(String phone) {
            entity.setPotentialRecipientPhone(phone);
            return this;
        }

        public ReferralEntityBuilder potentialRecipientDob(LocalDate dob) {
            entity.setPotentialRecipientDob(dob);
            return this;
        }

        public ReferralEntityBuilder potentialRecipientSsn(String ssn) {
            entity.setPotentialRecipientSsn(ssn);
            return this;
        }

        public ReferralEntityBuilder streetAddress(String address) {
            entity.setStreetAddress(address);
            return this;
        }

        public ReferralEntityBuilder city(String city) {
            entity.setCity(city);
            return this;
        }

        public ReferralEntityBuilder state(String state) {
            entity.setState(state);
            return this;
        }

        public ReferralEntityBuilder zipCode(String zipCode) {
            entity.setZipCode(zipCode);
            return this;
        }

        public ReferralEntityBuilder countyName(String countyName) {
            entity.setCountyName(countyName);
            return this;
        }

        public ReferralEntityBuilder preferredLanguage(String language) {
            entity.setPreferredLanguage(language);
            return this;
        }

        public ReferralEntityBuilder interpreterNeeded(Boolean needed) {
            entity.setInterpreterNeeded(needed);
            return this;
        }

        public ReferralEntityBuilder inquiryType(String type) {
            entity.setInquiryType(type);
            return this;
        }

        public ReferralEntityBuilder inquiryNotes(String notes) {
            entity.setInquiryNotes(notes);
            return this;
        }

        public ReferralEntityBuilder potentialRecipientAddress(String address) {
            entity.setStreetAddress(address);
            return this;
        }

        public ReferralEntityBuilder contactRelationship(String relationship) {
            entity.setContactRelationship(relationship);
            return this;
        }

        public ReferralEntityBuilder reasonForReferral(String reason) {
            // Store in inquiryNotes field
            if (entity.getInquiryNotes() == null) {
                entity.setInquiryNotes("Reason: " + reason);
            } else {
                entity.setInquiryNotes(entity.getInquiryNotes() + "\nReason: " + reason);
            }
            return this;
        }

        public ReferralEntityBuilder servicesNeeded(String services) {
            // Store in inquiryNotes field
            if (entity.getInquiryNotes() == null) {
                entity.setInquiryNotes("Services Needed: " + services);
            } else {
                entity.setInquiryNotes(entity.getInquiryNotes() + "\nServices Needed: " + services);
            }
            return this;
        }

        public ReferralEntityBuilder urgencyDescription(String description) {
            // Store in inquiryNotes field
            if (entity.getInquiryNotes() == null) {
                entity.setInquiryNotes("Urgency: " + description);
            } else {
                entity.setInquiryNotes(entity.getInquiryNotes() + "\nUrgency: " + description);
            }
            return this;
        }

        public ReferralEntityBuilder notes(String notes) {
            // Append to inquiryNotes field
            if (entity.getInquiryNotes() == null) {
                entity.setInquiryNotes(notes);
            } else {
                entity.setInquiryNotes(entity.getInquiryNotes() + "\n" + notes);
            }
            return this;
        }

        public ReferralEntity build() {
            return entity;
        }
    }
}
