package com.example.kafkaeventdrivenapp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Base64;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * AuthControllerTest
 * 
 * NOTE: Authentication is now handled by sajeevs-codebase-main's Keycloak.
 * These tests verify that the controller returns appropriate messages.
 */
class AuthControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        AuthController controller = new AuthController();
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void loginReturnsBadRequestWithMessage() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("username", "provider_user", "password", "secure-pass")
                        )))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("sajeevs-codebase-main")));
    }

    @Test
    void logoutReturnsOk() throws Exception {
        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer dummy-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("refreshToken", "refresh-token"))))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Keycloak")));
    }

    @Test
    void getUserInfoReturnsUserDetailsFromJWT() throws Exception {
        // Create a mock JWT token with user info
        String jwtPayload = "{\"preferred_username\":\"testuser\",\"realm_access\":{\"roles\":[\"CASE_WORKER\"]}}";
        String jwtToken = "header." + Base64.getUrlEncoder().encodeToString(jwtPayload.getBytes()) + ".signature";

        mockMvc.perform(get("/api/auth/user-info")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("username")));
    }

    @Test
    void getUserInfoReturnsBadRequestForInvalidToken() throws Exception {
        mockMvc.perform(get("/api/auth/user-info")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isBadRequest());
    }
}

