package com.example.kafkaeventdrivenapp.util;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Utility class for setting up security context in tests
 */
public class TestSecurityUtils {
    
    /**
     * Set up simple authentication with a role
     */
    public static void setAuthentication(String role) {
        Authentication auth = new UsernamePasswordAuthenticationToken(
            "test-user",
            null,
            List.of(new SimpleGrantedAuthority("ROLE_" + role))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
    
    /**
     * Set up JWT-based authentication with a role
     */
    public static void setJwtAuthentication(String role) {
        Jwt jwt = Jwt.withTokenValue("test-token")
            .header("alg", "RS256")
            .claim("realm_access", Map.of("roles", List.of(role)))
            .claim("preferred_username", "test-user")
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(3600))
            .build();
        
        Authentication auth = new org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken(jwt);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
    
    /**
     * Clear authentication context
     */
    public static void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }
}

