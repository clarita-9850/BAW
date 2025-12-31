package com.example.externalvalidationapi.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Service
public class SsnDatasetService {

    private Map<String, String> ssnDataset = new HashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    public void loadSsnDataset() {
        try {
            ClassPathResource resource = new ClassPathResource("ssn-dataset.json");
            InputStream inputStream = resource.getInputStream();
            ssnDataset = objectMapper.readValue(inputStream, new TypeReference<Map<String, String>>() {});
            System.out.println("✅ Loaded SSN dataset from file: " + ssnDataset.size() + " records");
        } catch (IOException e) {
            System.err.println("❌ Error loading SSN dataset from file: " + e.getMessage());
            e.printStackTrace();
            // Initialize with empty map if file loading fails
            ssnDataset = new HashMap<>();
        }
    }

    public Map<String, String> getSsnDataset() {
        return ssnDataset;
    }

    public String getFirstNameForSsn(String ssn) {
        return ssnDataset.get(ssn);
    }

    public boolean containsSsn(String ssn) {
        return ssnDataset.containsKey(ssn);
    }
}

