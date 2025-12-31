package com.example.notificationservice.service;

import com.example.notificationservice.model.NotificationRequest;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@Service
public class SmsService {

    @Value("${twilio.account.sid:AC1234567890abcdef1234567890abcdef}")
    private String accountSid;

    @Value("${twilio.auth.token:your_auth_token}")
    private String authToken;

    @Value("${twilio.phone.number:+1234567890}")
    private String fromPhoneNumber;

    @PostConstruct
    public void init() {
        Twilio.init(accountSid, authToken);
    }

    public void sendSms(NotificationRequest request) {
        try {
            Message message = Message.creator(
                    new PhoneNumber(request.getRecipientPhone()),
                    new PhoneNumber(fromPhoneNumber),
                    request.getMessage()
            ).create();

            System.out.println("SMS sent successfully to: " + request.getRecipientPhone() + 
                             " with SID: " + message.getSid());
        } catch (Exception e) {
            System.err.println("Failed to send SMS: " + e.getMessage());
        }
    }
}
