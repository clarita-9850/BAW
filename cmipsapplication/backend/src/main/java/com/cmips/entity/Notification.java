package com.cmips.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    private NotificationType notificationType = NotificationType.INFO;

    @Column(name = "read_status")
    private Boolean readStatus = false;

    @Column(name = "action_link")
    private String actionLink;

    @Column(name = "related_entity_type")
    private String relatedEntityType;

    @Column(name = "related_entity_id")
    private Long relatedEntityId;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public Notification() {}

    public Notification(Long id, String userId, String message, NotificationType notificationType,
                       Boolean readStatus, String actionLink, String relatedEntityType,
                       Long relatedEntityId, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.message = message;
        this.notificationType = notificationType;
        this.readStatus = readStatus;
        this.actionLink = actionLink;
        this.relatedEntityType = relatedEntityType;
        this.relatedEntityId = relatedEntityId;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public NotificationType getNotificationType() { return notificationType; }
    public void setNotificationType(NotificationType notificationType) { this.notificationType = notificationType; }

    public Boolean getReadStatus() { return readStatus; }
    public void setReadStatus(Boolean readStatus) { this.readStatus = readStatus; }

    public String getActionLink() { return actionLink; }
    public void setActionLink(String actionLink) { this.actionLink = actionLink; }

    public String getRelatedEntityType() { return relatedEntityType; }
    public void setRelatedEntityType(String relatedEntityType) { this.relatedEntityType = relatedEntityType; }

    public Long getRelatedEntityId() { return relatedEntityId; }
    public void setRelatedEntityId(Long relatedEntityId) { this.relatedEntityId = relatedEntityId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // Builder pattern
    public static NotificationBuilder builder() { return new NotificationBuilder(); }

    public static class NotificationBuilder {
        private Long id;
        private String userId;
        private String message;
        private NotificationType notificationType = NotificationType.INFO;
        private Boolean readStatus = false;
        private String actionLink;
        private String relatedEntityType;
        private Long relatedEntityId;
        private LocalDateTime createdAt = LocalDateTime.now();

        public NotificationBuilder id(Long id) { this.id = id; return this; }
        public NotificationBuilder userId(String userId) { this.userId = userId; return this; }
        public NotificationBuilder message(String message) { this.message = message; return this; }
        public NotificationBuilder notificationType(NotificationType notificationType) { this.notificationType = notificationType; return this; }
        public NotificationBuilder readStatus(Boolean readStatus) { this.readStatus = readStatus; return this; }
        public NotificationBuilder actionLink(String actionLink) { this.actionLink = actionLink; return this; }
        public NotificationBuilder relatedEntityType(String relatedEntityType) { this.relatedEntityType = relatedEntityType; return this; }
        public NotificationBuilder relatedEntityId(Long relatedEntityId) { this.relatedEntityId = relatedEntityId; return this; }
        public NotificationBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public Notification build() {
            return new Notification(id, userId, message, notificationType, readStatus,
                                   actionLink, relatedEntityType, relatedEntityId, createdAt);
        }
    }

    public enum NotificationType {
        INFO, WARNING, ALERT, SUCCESS
    }
}
