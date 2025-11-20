package com.cmips.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String username;
    
    @Column(nullable = false)
    private String passwordHash;
    
    @Column(nullable = false)
    private String role;
    
    @Column
    private String email;
    
    @Column
    private String firstName;
    
    @Column
    private String lastName;
    
    @Column
    private String department;
    
    @Column
    private String location;
    
    @Column
    private Boolean active = true;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Default constructor
    public User() {}
    
    // Constructors for different use cases
    public User(String username, String passwordHash, String role) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.active = true;
    }
    
    public User(String username, String passwordHash, String role, String email, String firstName, String lastName) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.active = true;
    }
    
    public User(String username, String passwordHash, String role, String email, String firstName, String lastName, String department, String location) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.department = department;
        this.location = location;
        this.active = true;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    
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
