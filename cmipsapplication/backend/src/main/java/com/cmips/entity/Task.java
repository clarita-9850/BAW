package com.cmips.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "tasks")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Task {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private TaskStatus status = TaskStatus.OPEN;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private TaskPriority priority = TaskPriority.MEDIUM;
    
    @Column(name = "assigned_to", nullable = false)
    private String assignedTo;
    
    @Column(name = "created_by")
    private String createdBy;
    
    private LocalDateTime dueDate;
    
    private LocalDateTime escalationDate;
    
    @Column(name = "related_entity_type")
    private String relatedEntityType;
    
    @Column(name = "related_entity_id")
    private Long relatedEntityId;
    
    @Column(name = "work_queue")
    private String workQueue;  // e.g., "PROVIDER_MANAGEMENT", "PAYROLL_APPROVAL"
    
    @Column(name = "queue_role")
    private String queueRole;  // Role required to access this queue
    
    @Column(name = "trigger_condition", columnDefinition = "TEXT")
    private String triggerCondition;
    
    @Column(name = "auto_close_on")
    private String autoCloseOn;
    
    @Column(name = "action_link")
    private String actionLink;
    
    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    public enum TaskStatus {
        OPEN, IN_PROGRESS, CLOSED, ESCALATED
    }
    
    public enum TaskPriority {
        HIGH, MEDIUM, LOW
    }
}



