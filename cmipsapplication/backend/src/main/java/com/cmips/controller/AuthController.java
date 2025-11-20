package com.cmips.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Value("${keycloak.auth-server-url}")
    private String keycloakAuthServerUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Autowired
    private RestTemplate restTemplate;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest) {
        try {
            String username = loginRequest.get("username");
            String password = loginRequest.get("password");

            if (username == null || password == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Username and password are required"));
            }

            // Prepare Keycloak token request
            String tokenUrl = keycloakAuthServerUrl + "realms/" + realm + "/protocol/openid-connect/token";
            
            String requestBody = "username=" + username +
                               "&password=" + password +
                               "&grant_type=password" +
                               "&client_id=cmips-backend" +
                               "&client_secret=X6282J5tQzu2tzqLcglKmjhwfidB0vh9";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

            // Call Keycloak
            ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> tokenResponse = response.getBody();
                return ResponseEntity.ok(tokenResponse);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid credentials"));
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Login failed: " + e.getMessage()));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> request) {
        try {
            String refreshToken = request.get("refresh_token");

            if (refreshToken == null || refreshToken.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Refresh token is required"));
            }

            // Prepare Keycloak refresh token request
            String tokenUrl = keycloakAuthServerUrl + "realms/" + realm + "/protocol/openid-connect/token";
            
            String requestBody = "grant_type=refresh_token" +
                               "&refresh_token=" + refreshToken +
                               "&client_id=cmips-backend" +
                               "&client_secret=X6282J5tQzu2tzqLcglKmjhwfidB0vh9";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

            // Call Keycloak
            ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> tokenResponse = response.getBody();
                return ResponseEntity.ok(tokenResponse);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Token refresh failed"));
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Token refresh failed: " + e.getMessage()));
        }
    }
}
