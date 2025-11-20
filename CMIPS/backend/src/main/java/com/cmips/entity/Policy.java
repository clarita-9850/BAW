package com.cmips.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "policies")
public class Policy {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String role;
    
    @Column(nullable = false)
    private String resource; // e.g., "timesheet", "user", "policy"
    
    @Column(nullable = false)
    private String action; // e.g., "GET", "POST", "PUT", "DELETE"
    
    @Column(nullable = false)
    private Boolean allowed; // true = allow, false = deny
    
    @Column
    private String description;
    
    @Column
    private Integer priority = 0; // Higher number = higher priority
    
    @Column
    private Boolean active = true;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Default constructor
    public Policy() {}
    
    // Constructors
    public Policy(String role, String resource, String action, Boolean allowed) {
        this.role = role;
        this.resource = resource;
        this.action = action;
        this.allowed = allowed;
        this.active = true;
    }
    
    public Policy(String role, String resource, String action, Boolean allowed, String description) {
        this.role = role;
        this.resource = resource;
        this.action = action;
        this.allowed = allowed;
        this.description = description;
        this.active = true;
    }
    
    public Policy(String role, String resource, String action, Boolean allowed, String description, Integer priority) {
        this.role = role;
        this.resource = resource;
        this.action = action;
        this.allowed = allowed;
        this.description = description;
        this.priority = priority;
        this.active = true;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    
    public String getResource() { return resource; }
    public void setResource(String resource) { this.resource = resource; }
    
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    
    public Boolean getAllowed() { return allowed; }
    public void setAllowed(Boolean allowed) { this.allowed = allowed; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }
    
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
