package com.cmips.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "timesheets")
public class Timesheet {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;
    
    @Column(nullable = false)
    private LocalDate date;
    
    @Column(nullable = false)
    private Double hours;
    
    @Column
    private String description;
    
    @Column
    private String status = "SUBMITTED"; // SUBMITTED, APPROVED, REJECTED, REVISION_REQUESTED
    
    @Column
    private String comments;
    
    @Column(name = "approved_by")
    private Long approvedBy;
    
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;
    
    @Column(name = "rejection_reason")
    private String rejectionReason;
    
    @Column(name = "revision_count")
    private Integer revisionCount = 0;
    
    @Column(name = "last_revision_date")
    private LocalDateTime lastRevisionDate;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Default constructor
    public Timesheet() {}
    
    // Constructors
    public Timesheet(Long userId, LocalDate date, Double hours) {
        this.userId = userId;
        this.date = date;
        this.hours = hours;
        this.status = "SUBMITTED";
        this.revisionCount = 0;
    }
    
    public Timesheet(Long userId, LocalDate date, Double hours, String description) {
        this.userId = userId;
        this.date = date;
        this.hours = hours;
        this.description = description;
        this.status = "SUBMITTED";
        this.revisionCount = 0;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    
    public Double getHours() { return hours; }
    public void setHours(Double hours) { this.hours = hours; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }
    
    public Long getApprovedBy() { return approvedBy; }
    public void setApprovedBy(Long approvedBy) { this.approvedBy = approvedBy; }
    
    public LocalDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }
    
    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
    
    public Integer getRevisionCount() { return revisionCount; }
    public void setRevisionCount(Integer revisionCount) { this.revisionCount = revisionCount; }
    
    public LocalDateTime getLastRevisionDate() { return lastRevisionDate; }
    public void setLastRevisionDate(LocalDateTime lastRevisionDate) { this.lastRevisionDate = lastRevisionDate; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
