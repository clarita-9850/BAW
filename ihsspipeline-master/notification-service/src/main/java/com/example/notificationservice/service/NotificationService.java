package com.example.notificationservice.service;

import com.example.notificationservice.model.NotificationRequest;
import com.example.notificationservice.model.NotificationType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    @Autowired
    private EmailService emailService;

    @Autowired
    private SmsService smsService;

    public void sendNotification(NotificationRequest request) {
        System.out.println("Processing notification request: " + request.getEventType());
        
        switch (request.getType()) {
            case EMAIL:
                if (request.getRecipientEmail() != null && !request.getRecipientEmail().isEmpty()) {
                    emailService.sendEmail(request);
                }
                break;
            case SMS:
                if (request.getRecipientPhone() != null && !request.getRecipientPhone().isEmpty()) {
                    smsService.sendSms(request);
                }
                break;
            case BOTH:
                if (request.getRecipientEmail() != null && !request.getRecipientEmail().isEmpty()) {
                    emailService.sendEmail(request);
                }
                if (request.getRecipientPhone() != null && !request.getRecipientPhone().isEmpty()) {
                    smsService.sendSms(request);
                }
                break;
        }
    }
}
