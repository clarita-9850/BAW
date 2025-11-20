package com.cmips.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private String userId;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;
    
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private NotificationType notificationType = NotificationType.INFO;
    
    @Column(name = "read_status")
    @Builder.Default
    private Boolean readStatus = false;
    
    @Column(name = "action_link")
    private String actionLink;
    
    @Column(name = "related_entity_type")
    private String relatedEntityType;
    
    @Column(name = "related_entity_id")
    private Long relatedEntityId;
    
    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    public enum NotificationType {
        INFO, WARNING, ALERT, SUCCESS
    }
}



