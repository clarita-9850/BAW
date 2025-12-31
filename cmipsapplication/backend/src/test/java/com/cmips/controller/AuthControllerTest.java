package com.cmips.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthController login authentication
 * 
 * These tests verify:
 * - Successful login scenarios
 * - Failed login scenarios (invalid credentials)
 * - Validation errors (missing username/password)
 * - Exception handling
 * - Token response structure
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController Login Authentication Tests")
class AuthControllerTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private AuthController authController;

    private ObjectMapper objectMapper;
    
    private static final String KEYCLOAK_AUTH_SERVER_URL = "http://localhost:8085/auth/";
    private static final String REALM = "cmips";
    private static final String VALID_USERNAME = "testuser";
    private static final String VALID_PASSWORD = "testpassword";
    private static final String ACCESS_TOKEN = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0dXNlciIsInJvbGUiOiJVU0VSIn0.test";
    private static final String REFRESH_TOKEN = "refresh_token_value";

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        
        // Inject mock values using ReflectionTestUtils
        ReflectionTestUtils.setField(authController, "keycloakAuthServerUrl", KEYCLOAK_AUTH_SERVER_URL);
        ReflectionTestUtils.setField(authController, "realm", REALM);
        ReflectionTestUtils.setField(authController, "restTemplate", restTemplate);
    }

    @Test
    @DisplayName("Should successfully login with valid credentials")
    void testSuccessfulLogin() {
        // Arrange
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("username", VALID_USERNAME);
        loginRequest.put("password", VALID_PASSWORD);

        Map<String, Object> keycloakResponse = new HashMap<>();
        keycloakResponse.put("access_token", ACCESS_TOKEN);
        keycloakResponse.put("refresh_token", REFRESH_TOKEN);
        keycloakResponse.put("expires_in", 3600);
        keycloakResponse.put("token_type", "Bearer");

        ResponseEntity<Map> mockResponse = new ResponseEntity<>(keycloakResponse, HttpStatus.OK);

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(mockResponse);

        // Act
        ResponseEntity<?> response = authController.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertEquals(ACCESS_TOKEN, responseBody.get("access_token"));
        assertEquals(REFRESH_TOKEN, responseBody.get("refresh_token"));
        assertEquals(3600, responseBody.get("expires_in"));
        assertEquals("Bearer", responseBody.get("token_type"));

        // Verify RestTemplate was called with correct parameters
        verify(restTemplate, times(1)).postForEntity(
                eq(KEYCLOAK_AUTH_SERVER_URL + "realms/" + REALM + "/protocol/openid-connect/token"),
                any(HttpEntity.class),
                eq(Map.class)
        );
    }

    @Test
    @DisplayName("Should return BadRequest when username is missing")
    void testLoginWithMissingUsername() {
        // Arrange
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("password", VALID_PASSWORD);

        // Act
        ResponseEntity<?> response = authController.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        
        @SuppressWarnings("unchecked")
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertTrue(responseBody.containsKey("error"));
        assertEquals("Username and password are required", responseBody.get("error"));

        // Verify RestTemplate was NOT called
        verify(restTemplate, never()).postForEntity(anyString(), any(), any());
    }

    @Test
    @DisplayName("Should return BadRequest when password is missing")
    void testLoginWithMissingPassword() {
        // Arrange
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("username", VALID_USERNAME);

        // Act
        ResponseEntity<?> response = authController.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        
        @SuppressWarnings("unchecked")
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertTrue(responseBody.containsKey("error"));
        assertEquals("Username and password are required", responseBody.get("error"));

        // Verify RestTemplate was NOT called
        verify(restTemplate, never()).postForEntity(anyString(), any(), any());
    }

    @Test
    @DisplayName("Should return BadRequest when both username and password are missing")
    void testLoginWithMissingCredentials() {
        // Arrange
        Map<String, String> loginRequest = new HashMap<>();

        // Act
        ResponseEntity<?> response = authController.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        
        @SuppressWarnings("unchecked")
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertTrue(responseBody.containsKey("error"));
        assertEquals("Username and password are required", responseBody.get("error"));

        // Verify RestTemplate was NOT called
        verify(restTemplate, never()).postForEntity(anyString(), any(), any());
    }

    @Test
    @DisplayName("Should return Unauthorized when credentials are invalid")
    void testLoginWithInvalidCredentials() {
        // Arrange
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("username", VALID_USERNAME);
        loginRequest.put("password", "wrongpassword");

        ResponseEntity<Map> mockResponse = new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(mockResponse);

        // Act
        ResponseEntity<?> response = authController.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        
        @SuppressWarnings("unchecked")
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertTrue(responseBody.containsKey("error"));
        assertEquals("Invalid credentials", responseBody.get("error"));

        // Verify RestTemplate was called
        verify(restTemplate, times(1)).postForEntity(anyString(), any(HttpEntity.class), eq(Map.class));
    }

    @Test
    @DisplayName("Should return Unauthorized when Keycloak returns non-OK status")
    void testLoginWithKeycloakError() {
        // Arrange
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("username", VALID_USERNAME);
        loginRequest.put("password", VALID_PASSWORD);

        ResponseEntity<Map> mockResponse = new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(mockResponse);

        // Act
        ResponseEntity<?> response = authController.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        
        @SuppressWarnings("unchecked")
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertTrue(responseBody.containsKey("error"));
        assertEquals("Invalid credentials", responseBody.get("error"));
    }

    @Test
    @DisplayName("Should handle RestClientException gracefully")
    void testLoginWithRestClientException() {
        // Arrange
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("username", VALID_USERNAME);
        loginRequest.put("password", VALID_PASSWORD);

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenThrow(new RestClientException("Connection refused"));

        // Act
        ResponseEntity<?> response = authController.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        
        @SuppressWarnings("unchecked")
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertTrue(responseBody.containsKey("error"));
        assertTrue(responseBody.get("error").contains("Login failed"));
        assertTrue(responseBody.get("error").contains("Connection refused"));
    }

    @Test
    @DisplayName("Should handle generic Exception gracefully")
    void testLoginWithGenericException() {
        // Arrange
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("username", VALID_USERNAME);
        loginRequest.put("password", VALID_PASSWORD);

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenThrow(new RuntimeException("Unexpected error"));

        // Act
        ResponseEntity<?> response = authController.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        
        @SuppressWarnings("unchecked")
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertTrue(responseBody.containsKey("error"));
        assertTrue(responseBody.get("error").contains("Login failed"));
        assertTrue(responseBody.get("error").contains("Unexpected error"));
    }

    @Test
    @DisplayName("Should construct correct Keycloak token URL")
    void testKeycloakTokenUrlConstruction() {
        // Arrange
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("username", VALID_USERNAME);
        loginRequest.put("password", VALID_PASSWORD);

        Map<String, Object> keycloakResponse = new HashMap<>();
        keycloakResponse.put("access_token", ACCESS_TOKEN);

        ResponseEntity<Map> mockResponse = new ResponseEntity<>(keycloakResponse, HttpStatus.OK);

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(mockResponse);

        // Act
        authController.login(loginRequest);

        // Assert
        String expectedUrl = KEYCLOAK_AUTH_SERVER_URL + "realms/" + REALM + "/protocol/openid-connect/token";
        verify(restTemplate, times(1)).postForEntity(
                eq(expectedUrl),
                any(HttpEntity.class),
                eq(Map.class)
        );
    }

    @Test
    @DisplayName("Should set correct Content-Type header")
    void testRequestHeaders() {
        // Arrange
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("username", VALID_USERNAME);
        loginRequest.put("password", VALID_PASSWORD);

        Map<String, Object> keycloakResponse = new HashMap<>();
        keycloakResponse.put("access_token", ACCESS_TOKEN);

        ResponseEntity<Map> mockResponse = new ResponseEntity<>(keycloakResponse, HttpStatus.OK);

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenAnswer(invocation -> {
                    HttpEntity<?> entity = invocation.getArgument(1);
                    HttpHeaders headers = entity.getHeaders();
                    
                    // Verify Content-Type is set correctly
                    assertEquals(MediaType.APPLICATION_FORM_URLENCODED, headers.getContentType());
                    
                    return mockResponse;
                });

        // Act
        authController.login(loginRequest);

        // Assert - verification is done in the thenAnswer callback
        verify(restTemplate, times(1)).postForEntity(anyString(), any(HttpEntity.class), eq(Map.class));
    }

    @Test
    @DisplayName("Should include correct request body parameters")
    void testRequestBodyParameters() {
        // Arrange
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("username", VALID_USERNAME);
        loginRequest.put("password", VALID_PASSWORD);

        Map<String, Object> keycloakResponse = new HashMap<>();
        keycloakResponse.put("access_token", ACCESS_TOKEN);

        ResponseEntity<Map> mockResponse = new ResponseEntity<>(keycloakResponse, HttpStatus.OK);

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenAnswer(invocation -> {
                    HttpEntity<?> entity = invocation.getArgument(1);
                    String body = (String) entity.getBody();
                    
                    // Verify request body contains required parameters
                    assertNotNull(body);
                    assertTrue(body.contains("username=" + VALID_USERNAME));
                    assertTrue(body.contains("password=" + VALID_PASSWORD));
                    assertTrue(body.contains("grant_type=password"));
                    assertTrue(body.contains("client_id=cmips-backend"));
                    assertTrue(body.contains("client_secret="));
                    
                    return mockResponse;
                });

        // Act
        authController.login(loginRequest);

        // Assert - verification is done in the thenAnswer callback
        verify(restTemplate, times(1)).postForEntity(anyString(), any(HttpEntity.class), eq(Map.class));
    }

    @Test
    @DisplayName("Should handle empty username")
    void testLoginWithEmptyUsername() {
        // Arrange
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("username", "");
        loginRequest.put("password", VALID_PASSWORD);

        // Act
        ResponseEntity<?> response = authController.login(loginRequest);

        // Assert
        // Empty string is not null, so it will proceed to Keycloak
        // But we should verify the request is made
        verify(restTemplate, atLeastOnce()).postForEntity(anyString(), any(HttpEntity.class), eq(Map.class));
    }

    @Test
    @DisplayName("Should handle empty password")
    void testLoginWithEmptyPassword() {
        // Arrange
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("username", VALID_USERNAME);
        loginRequest.put("password", "");

        // Act
        ResponseEntity<?> response = authController.login(loginRequest);

        // Assert
        // Empty string is not null, so it will proceed to Keycloak
        // But we should verify the request is made
        verify(restTemplate, atLeastOnce()).postForEntity(anyString(), any(HttpEntity.class), eq(Map.class));
    }

    @Test
    @DisplayName("Should handle null username in request")
    void testLoginWithNullUsername() {
        // Arrange
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("username", null);
        loginRequest.put("password", VALID_PASSWORD);

        // Act
        ResponseEntity<?> response = authController.login(loginRequest);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(restTemplate, never()).postForEntity(anyString(), any(), any());
    }

    @Test
    @DisplayName("Should handle null password in request")
    void testLoginWithNullPassword() {
        // Arrange
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("username", VALID_USERNAME);
        loginRequest.put("password", null);

        // Act
        ResponseEntity<?> response = authController.login(loginRequest);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(restTemplate, never()).postForEntity(anyString(), any(), any());
    }
}







