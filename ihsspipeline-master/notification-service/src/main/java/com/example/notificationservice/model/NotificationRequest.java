package com.example.notificationservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {
    private String recipientEmail;
    private String recipientPhone;
    private String subject;
    private String message;
    private NotificationType type;
    private String eventType;
    private String eventData;
}
