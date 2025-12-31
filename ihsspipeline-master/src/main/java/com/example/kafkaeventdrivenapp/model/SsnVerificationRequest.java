package com.example.kafkaeventdrivenapp.model;

import lombok.Data;

@Data
public class SsnVerificationRequest {
    private String firstName;
    private String ssn;
}

