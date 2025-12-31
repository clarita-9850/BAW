package com.example.notificationservice.controller;

import com.example.notificationservice.model.NotificationRequest;
import com.example.notificationservice.model.NotificationType;
import com.example.notificationservice.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @PostMapping("/test")
    public String testNotification(@RequestBody NotificationRequest request) {
        try {
            notificationService.sendNotification(request);
            return "Notification sent successfully!";
        } catch (Exception e) {
            return "Failed to send notification: " + e.getMessage();
        }
    }

    @GetMapping("/test-email")
    public String testEmail() {
        NotificationRequest request = new NotificationRequest();
        request.setRecipientEmail("test@example.com");
        request.setSubject("Test Email");
        request.setMessage("This is a test email from the notification service.");
        request.setType(NotificationType.EMAIL);
        request.setEventType("TEST");
        request.setEventData("{}");
        
        notificationService.sendNotification(request);
        return "Test email notification sent!";
    }

    @GetMapping("/test-sms")
    public String testSms() {
        NotificationRequest request = new NotificationRequest();
        request.setRecipientPhone("+1234567890");
        request.setMessage("This is a test SMS from the notification service.");
        request.setType(NotificationType.SMS);
        request.setEventType("TEST");
        request.setEventData("{}");
        
        notificationService.sendNotification(request);
        return "Test SMS notification sent!";
    }
}
