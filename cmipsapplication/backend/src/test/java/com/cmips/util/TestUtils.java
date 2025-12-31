package com.cmips.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Test utility class for common test setup operations
 */
public class TestUtils {

    /**
     * Create a mock JWT token string for testing
     */
    public static String createMockJwtToken(String role, String county) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "test-user-id");
        claims.put("preferred_username", "testuser");
        claims.put("realm_access", Map.of("roles", List.of(role)));
        if (county != null) {
            claims.put("groups", List.of(county));
            claims.put("countyId", county);
        }
        
        // Create a simple mock JWT structure
        String header = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
        String payload = claims.toString();
        return "header." + java.util.Base64.getUrlEncoder().encodeToString(payload.getBytes()) + ".signature";
    }

    /**
     * Setup mock security context with JWT
     */
    public static void setupSecurityContext(String username, List<String> roles, String county) {
        Authentication authentication = org.mockito.Mockito.mock(Authentication.class);
        Jwt jwt = org.mockito.Mockito.mock(Jwt.class);
        SecurityContext securityContext = org.mockito.Mockito.mock(SecurityContext.class);
        
        Map<String, Object> realmAccess = new HashMap<>();
        realmAccess.put("roles", roles);
        
        org.mockito.Mockito.when(jwt.getClaimAsString("preferred_username")).thenReturn(username);
        org.mockito.Mockito.when(jwt.getSubject()).thenReturn("user-id-" + username);
        org.mockito.Mockito.when(jwt.getClaimAsMap("realm_access")).thenReturn(realmAccess);
        if (county != null) {
            org.mockito.Mockito.when(jwt.getClaimAsStringList("groups")).thenReturn(List.of(county));
        }
        
        org.mockito.Mockito.when(authentication.getPrincipal()).thenReturn(jwt);
        org.mockito.Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    /**
     * Clear security context after tests
     */
    public static void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }
}







