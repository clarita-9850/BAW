package com.cmips.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "roles")
public class Role {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String name; // e.g., "CASE_WORKER", "AUDITOR", "MANAGER"
    
    @Column
    private String displayName; // e.g., "Case Worker", "Auditor", "Manager"
    
    @Column
    private String description; // Description of the role
    
    @Column
    private String department; // Department this role belongs to
    
    @Column
    private String location; // Location/office for this role
    
    @Column
    private Boolean active = true; // Whether the role is active
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Default constructor
    public Role() {}
    
    // Constructors
    public Role(String name, String displayName) {
        this.name = name;
        this.displayName = displayName;
        this.active = true;
    }
    
    public Role(String name, String displayName, String description) {
        this.name = name;
        this.displayName = displayName;
        this.description = description;
        this.active = true;
    }
    
    public Role(String name, String displayName, String description, String department, String location) {
        this.name = name;
        this.displayName = displayName;
        this.description = description;
        this.department = department;
        this.location = location;
        this.active = true;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}


