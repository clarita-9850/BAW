package com.example.kafkaeventdrivenapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * Service for fetching user information from Keycloak
 */
@Service
public class KeycloakUserService {
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Value("${keycloak.auth-server-url:http://cmips-keycloak:8080}")
    private String keycloakAuthServerUrl;
    
    @Value("${keycloak.realm:cmips}")
    private String keycloakRealm;
    
    @Value("${KEYCLOAK_ADMIN_USER:admin}")
    private String keycloakAdminUser;
    
    @Value("${KEYCLOAK_ADMIN_PASSWORD:admin123}")
    private String keycloakAdminPassword;
    
    private String adminToken = null;
    private long tokenExpiryTime = 0;
    
    /**
     * Get admin token from Keycloak
     */
    private String getAdminToken() {
        // Check if token is still valid (refresh if expired or about to expire in 5 minutes)
        if (adminToken != null && System.currentTimeMillis() < tokenExpiryTime - 300000) {
            return adminToken;
        }
        
        try {
            String tokenUrl = keycloakAuthServerUrl + "/realms/master/protocol/openid-connect/token";
            String requestBody = "username=" + keycloakAdminUser +
                               "&password=" + keycloakAdminPassword +
                               "&grant_type=password" +
                               "&client_id=admin-cli";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED);
            
            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
            
            @SuppressWarnings("unchecked")
            ResponseEntity<Map<String, Object>> response = (ResponseEntity<Map<String, Object>>) (ResponseEntity<?>) restTemplate.postForEntity(tokenUrl, entity, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                adminToken = (String) response.getBody().get("access_token");
                // Token typically expires in 60 seconds, but we'll refresh every 50 seconds to be safe
                tokenExpiryTime = System.currentTimeMillis() + 50000;
                return adminToken;
            }
            
            throw new RuntimeException("Failed to get admin token from Keycloak");
            
        } catch (Exception e) {
            System.err.println("❌ KeycloakUserService: Error getting admin token: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to get admin token", e);
        }
    }
    
    /**
     * Get user email from Keycloak by username
     */
    public String getUserEmailFromKeycloak(String username) {
        if (username == null || username.trim().isEmpty()) {
            return null;
        }
        
        try {
            String adminToken = getAdminToken();
            String url = keycloakAuthServerUrl + "/admin/realms/" + keycloakRealm + "/users?username=" + username;
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + adminToken);
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
            
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            
            @SuppressWarnings("unchecked")
            ResponseEntity<List<Map<String, Object>>> response = (ResponseEntity<List<Map<String, Object>>>) (ResponseEntity<?>) restTemplate.exchange(url, HttpMethod.GET, entity, List.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null && !response.getBody().isEmpty()) {
                Map<String, Object> user = response.getBody().get(0);
                String email = (String) user.get("email");
                System.out.println("✅ KeycloakUserService: Found email for user " + username + ": " + email);
                return email;
            }
            
            System.out.println("⚠️ KeycloakUserService: User " + username + " not found in Keycloak");
            return null;
            
        } catch (Exception e) {
            System.err.println("❌ KeycloakUserService: Error fetching email for user " + username + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Get supervisor email for a specific county code
     */
    public String getSupervisorEmailForCounty(String countyCode) {
        if (countyCode == null || countyCode.trim().isEmpty()) {
            return null;
        }
        
        String username = "supervisor_" + countyCode;
        return getUserEmailFromKeycloak(username);
    }
    
    /**
     * Get case worker email for a specific county code
     */
    public String getCaseWorkerEmailForCounty(String countyCode) {
        if (countyCode == null || countyCode.trim().isEmpty()) {
            return null;
        }
        
        String username = "caseworker_" + countyCode;
        return getUserEmailFromKeycloak(username);
    }
}

