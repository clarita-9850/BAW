package com.cmips.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "work_queue_subscriptions", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"username", "work_queue"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkQueueSubscription {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "username", nullable = false)
    private String username;
    
    @Column(name = "work_queue", nullable = false)
    private String workQueue;
    
    @Column(name = "subscribed_by")
    private String subscribedBy; // Supervisor who added this subscription
    
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
}

