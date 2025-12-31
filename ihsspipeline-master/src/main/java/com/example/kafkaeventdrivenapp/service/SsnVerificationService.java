package com.example.kafkaeventdrivenapp.service;

import com.example.kafkaeventdrivenapp.model.SsnVerificationRequest;
import com.example.kafkaeventdrivenapp.model.SsnVerificationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class SsnVerificationService {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${EXTERNAL_SSN_VALIDATION_URL:http://external-validation-api:8082/api/validation/ssn}")
    private String ssnValidationUrl;

    public SsnVerificationResponse verifySsn(SsnVerificationRequest request) {
        try {
            return restTemplate.postForObject(ssnValidationUrl, request, SsnVerificationResponse.class);
        } catch (Exception ex) {
            SsnVerificationResponse response = new SsnVerificationResponse();
            response.setValid(false);
            response.setStatus("ERROR");
            response.setMessage("SSN verification service unavailable: " + ex.getMessage());
            return response;
        }
    }
}

