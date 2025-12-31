package com.cmips.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tasks")
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
    private TaskStatus status = TaskStatus.OPEN;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
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
    private String workQueue;

    @Column(name = "queue_role")
    private String queueRole;

    @Column(name = "trigger_condition", columnDefinition = "TEXT")
    private String triggerCondition;

    @Column(name = "auto_close_on")
    private String autoCloseOn;

    @Column(name = "action_link")
    private String actionLink;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    public Task() {}

    public Task(Long id, String title, String description, TaskStatus status, TaskPriority priority,
                String assignedTo, String createdBy, LocalDateTime dueDate, LocalDateTime escalationDate,
                String relatedEntityType, Long relatedEntityId, String workQueue, String queueRole,
                String triggerCondition, String autoCloseOn, String actionLink, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
        this.priority = priority;
        this.assignedTo = assignedTo;
        this.createdBy = createdBy;
        this.dueDate = dueDate;
        this.escalationDate = escalationDate;
        this.relatedEntityType = relatedEntityType;
        this.relatedEntityId = relatedEntityId;
        this.workQueue = workQueue;
        this.queueRole = queueRole;
        this.triggerCondition = triggerCondition;
        this.autoCloseOn = autoCloseOn;
        this.actionLink = actionLink;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public TaskStatus getStatus() { return status; }
    public void setStatus(TaskStatus status) { this.status = status; }

    public TaskPriority getPriority() { return priority; }
    public void setPriority(TaskPriority priority) { this.priority = priority; }

    public String getAssignedTo() { return assignedTo; }
    public void setAssignedTo(String assignedTo) { this.assignedTo = assignedTo; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getDueDate() { return dueDate; }
    public void setDueDate(LocalDateTime dueDate) { this.dueDate = dueDate; }

    public LocalDateTime getEscalationDate() { return escalationDate; }
    public void setEscalationDate(LocalDateTime escalationDate) { this.escalationDate = escalationDate; }

    public String getRelatedEntityType() { return relatedEntityType; }
    public void setRelatedEntityType(String relatedEntityType) { this.relatedEntityType = relatedEntityType; }

    public Long getRelatedEntityId() { return relatedEntityId; }
    public void setRelatedEntityId(Long relatedEntityId) { this.relatedEntityId = relatedEntityId; }

    public String getWorkQueue() { return workQueue; }
    public void setWorkQueue(String workQueue) { this.workQueue = workQueue; }

    public String getQueueRole() { return queueRole; }
    public void setQueueRole(String queueRole) { this.queueRole = queueRole; }

    public String getTriggerCondition() { return triggerCondition; }
    public void setTriggerCondition(String triggerCondition) { this.triggerCondition = triggerCondition; }

    public String getAutoCloseOn() { return autoCloseOn; }
    public void setAutoCloseOn(String autoCloseOn) { this.autoCloseOn = autoCloseOn; }

    public String getActionLink() { return actionLink; }
    public void setActionLink(String actionLink) { this.actionLink = actionLink; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // Builder pattern
    public static TaskBuilder builder() { return new TaskBuilder(); }

    public static class TaskBuilder {
        private Long id;
        private String title;
        private String description;
        private TaskStatus status = TaskStatus.OPEN;
        private TaskPriority priority = TaskPriority.MEDIUM;
        private String assignedTo;
        private String createdBy;
        private LocalDateTime dueDate;
        private LocalDateTime escalationDate;
        private String relatedEntityType;
        private Long relatedEntityId;
        private String workQueue;
        private String queueRole;
        private String triggerCondition;
        private String autoCloseOn;
        private String actionLink;
        private LocalDateTime createdAt = LocalDateTime.now();
        private LocalDateTime updatedAt = LocalDateTime.now();

        public TaskBuilder id(Long id) { this.id = id; return this; }
        public TaskBuilder title(String title) { this.title = title; return this; }
        public TaskBuilder description(String description) { this.description = description; return this; }
        public TaskBuilder status(TaskStatus status) { this.status = status; return this; }
        public TaskBuilder priority(TaskPriority priority) { this.priority = priority; return this; }
        public TaskBuilder assignedTo(String assignedTo) { this.assignedTo = assignedTo; return this; }
        public TaskBuilder createdBy(String createdBy) { this.createdBy = createdBy; return this; }
        public TaskBuilder dueDate(LocalDateTime dueDate) { this.dueDate = dueDate; return this; }
        public TaskBuilder escalationDate(LocalDateTime escalationDate) { this.escalationDate = escalationDate; return this; }
        public TaskBuilder relatedEntityType(String relatedEntityType) { this.relatedEntityType = relatedEntityType; return this; }
        public TaskBuilder relatedEntityId(Long relatedEntityId) { this.relatedEntityId = relatedEntityId; return this; }
        public TaskBuilder workQueue(String workQueue) { this.workQueue = workQueue; return this; }
        public TaskBuilder queueRole(String queueRole) { this.queueRole = queueRole; return this; }
        public TaskBuilder triggerCondition(String triggerCondition) { this.triggerCondition = triggerCondition; return this; }
        public TaskBuilder autoCloseOn(String autoCloseOn) { this.autoCloseOn = autoCloseOn; return this; }
        public TaskBuilder actionLink(String actionLink) { this.actionLink = actionLink; return this; }
        public TaskBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public TaskBuilder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public Task build() {
            return new Task(id, title, description, status, priority, assignedTo, createdBy,
                           dueDate, escalationDate, relatedEntityType, relatedEntityId,
                           workQueue, queueRole, triggerCondition, autoCloseOn, actionLink,
                           createdAt, updatedAt);
        }
    }

    public enum TaskStatus {
        OPEN, PENDING, IN_PROGRESS, CLOSED, ESCALATED
    }

    public enum TaskPriority {
        HIGH, MEDIUM, LOW
    }
}
