package com.example.kafkaeventdrivenapp.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * AuthController
 * 
 * NOTE: Authentication is now handled by sajeevs-codebase-main's Keycloak integration.
 * This controller is kept for backward compatibility but returns appropriate messages.
 * 
 * For authentication, use sajeevs-codebase-main's frontend which redirects to Keycloak.
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> request) {
        // Authentication is handled by Keycloak directly from frontend
        // This endpoint is kept for backward compatibility
        return ResponseEntity.badRequest()
            .body(Map.of("error", "Authentication is handled by Keycloak. Please use the frontend login which calls Keycloak directly."));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(
            @RequestHeader("Authorization") String token,
            @RequestBody(required = false) LogoutRequest logoutRequest) {
        // Logout is handled by Keycloak session management
        return ResponseEntity.ok(Map.of("message", "Logout handled by Keycloak"));
    }

    @GetMapping("/user-info")
    public ResponseEntity<Map<String, Object>> getUserInfo(@RequestHeader("Authorization") String token) {
        try {
            // Extract user info directly from JWT token
            String jwtToken = token.replace("Bearer ", "");
            String[] parts = jwtToken.split("\\.");
            if (parts.length >= 2) {
                String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                com.fasterxml.jackson.databind.JsonNode jsonNode = mapper.readTree(payload);
                
                Map<String, Object> userInfo = new java.util.HashMap<>();
                if (jsonNode.has("preferred_username")) {
                    userInfo.put("username", jsonNode.get("preferred_username").asText());
                }
                if (jsonNode.has("realm_access")) {
                    userInfo.put("roles", jsonNode.get("realm_access").get("roles"));
                }
                
                return ResponseEntity.ok(userInfo);
            }
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid token"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Failed to get user info: " + e.getMessage()));
        }
    }

    // Request/Response classes
    public static class LoginRequest {
        private String username;
        private String password;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
    
    public static class LogoutRequest {
        private String refreshToken;

        public String getRefreshToken() {
            return refreshToken;
        }

        public void setRefreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
        }
    }
}
