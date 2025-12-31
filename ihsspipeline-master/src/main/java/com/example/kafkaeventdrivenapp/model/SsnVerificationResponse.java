package com.example.kafkaeventdrivenapp.model;

import lombok.Data;

@Data
public class SsnVerificationResponse {
    private boolean valid;
    private String status;
    private String message;
    private String matchedFirstName;
    private String matchedSsn;
}

