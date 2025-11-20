package com.cmips.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class PolicyRequest {
    
    @NotBlank(message = "Role is required")
    private String role;
    
    @NotBlank(message = "Resource is required")
    private String resource;
    
    @NotBlank(message = "Action is required")
    private String action;
    
    @NotNull(message = "Allowed is required")
    private Boolean allowed;
    
    private String description;
    
    private Integer priority = 0;
    
    private Boolean active = true;
    
    // Default constructor
    public PolicyRequest() {}
    
    // Constructor
    public PolicyRequest(String role, String resource, String action, Boolean allowed, String description, Integer priority, Boolean active) {
        this.role = role;
        this.resource = resource;
        this.action = action;
        this.allowed = allowed;
        this.description = description;
        this.priority = priority;
        this.active = active;
    }
    
    // Getters and Setters
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
}
