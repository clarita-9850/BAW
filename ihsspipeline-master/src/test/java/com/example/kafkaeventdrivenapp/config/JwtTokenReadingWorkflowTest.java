package com.example.kafkaeventdrivenapp.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for JWT Token Reading Workflow
 * 
 * This test verifies that the application correctly:
 * 1. Reads JWT tokens from sajeevs-codebase-main Keycloak
 * 2. Extracts roles from both client roles (resource_access.trial-app.roles) and realm roles (realm_access.roles)
 * 3. Converts roles to Spring Security authorities
 * 4. Handles fallback scenarios
 */
class JwtTokenReadingWorkflowTest {

    private SecurityConfig securityConfig;
    private JwtAuthenticationConverter jwtAuthenticationConverter;

    @BeforeEach
    void setUp() {
        securityConfig = new SecurityConfig();
        jwtAuthenticationConverter = securityConfig.jwtAuthenticationConverter();
    }

    @Test
    void testExtractClientRolesFromJWT() {
        // Create a JWT with client roles (resource_access.trial-app.roles)
        Map<String, Object> resourceAccess = new HashMap<>();
        Map<String, Object> trialAppAccess = new HashMap<>();
        trialAppAccess.put("roles", Arrays.asList("CASE_WORKER", "ADMIN"));
        resourceAccess.put("trial-app", trialAppAccess);

        Map<String, Object> claims = new HashMap<>();
        claims.put("resource_access", resourceAccess);
        claims.put("preferred_username", "testuser");

        Jwt jwt = createMockJwt(claims);

        // Extract authorities
        Collection<GrantedAuthority> authorities = jwtAuthenticationConverter
                .convert(jwt)
                .getAuthorities();

        // Verify client roles are extracted
        assertFalse(authorities.isEmpty(), "Authorities should not be empty");
        assertTrue(authorities.stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_CASE_WORKER") || 
                              a.getAuthority().equals("ROLE_ADMIN")),
                "Should contain CASE_WORKER or ADMIN role");
    }

    @Test
    void testExtractRealmRolesFromJWT() {
        // Create a JWT with realm roles only (no client roles)
        Map<String, Object> realmAccess = new HashMap<>();
        realmAccess.put("roles", Arrays.asList("SUPERVISOR", "offline_access", "uma_authorization"));

        Map<String, Object> claims = new HashMap<>();
        claims.put("realm_access", realmAccess);
        claims.put("preferred_username", "supervisor_user");

        Jwt jwt = createMockJwt(claims);

        // Extract authorities
        Collection<GrantedAuthority> authorities = jwtAuthenticationConverter
                .convert(jwt)
                .getAuthorities();

        // Verify realm roles are extracted (excluding default Keycloak roles)
        assertFalse(authorities.isEmpty(), "Authorities should not be empty");
        assertTrue(authorities.stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPERVISOR")),
                "Should contain SUPERVISOR role");
        
        // Verify default Keycloak roles are filtered out
        assertFalse(authorities.stream()
                .anyMatch(a -> a.getAuthority().contains("offline_access") || 
                              a.getAuthority().contains("uma_authorization")),
                "Should not contain default Keycloak roles");
    }

    @Test
    void testClientRolesTakePrecedenceOverRealmRoles() {
        // Create a JWT with both client and realm roles
        Map<String, Object> resourceAccess = new HashMap<>();
        Map<String, Object> trialAppAccess = new HashMap<>();
        trialAppAccess.put("roles", Arrays.asList("CASE_WORKER"));
        resourceAccess.put("trial-app", trialAppAccess);

        Map<String, Object> realmAccess = new HashMap<>();
        realmAccess.put("roles", Arrays.asList("SUPERVISOR"));

        Map<String, Object> claims = new HashMap<>();
        claims.put("resource_access", resourceAccess);
        claims.put("realm_access", realmAccess);
        claims.put("preferred_username", "testuser");

        Jwt jwt = createMockJwt(claims);

        // Extract authorities
        Collection<GrantedAuthority> authorities = jwtAuthenticationConverter
                .convert(jwt)
                .getAuthorities();

        // Verify client roles are used (CASE_WORKER), not realm roles (SUPERVISOR)
        assertTrue(authorities.stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_CASE_WORKER")),
                "Should contain CASE_WORKER from client roles");
        assertFalse(authorities.stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPERVISOR")),
                "Should not contain SUPERVISOR from realm roles when client roles exist");
    }

    @Test
    void testFallbackToPreferredUsername() {
        // Create a JWT with no roles, only preferred_username
        Map<String, Object> claims = new HashMap<>();
        claims.put("preferred_username", "caseworker1");

        Jwt jwt = createMockJwt(claims);

        // Extract authorities
        Collection<GrantedAuthority> authorities = jwtAuthenticationConverter
                .convert(jwt)
                .getAuthorities();

        // Verify fallback to username-based role mapping
        assertFalse(authorities.isEmpty(), "Authorities should not be empty");
        // RoleMapper should map "caseworker1" to a role
        assertTrue(authorities.size() > 0, "Should have at least one authority from username mapping");
    }

    @Test
    void testRoleCanonicalization() {
        // Test that role names are properly canonicalized
        Map<String, Object> resourceAccess = new HashMap<>();
        Map<String, Object> trialAppAccess = new HashMap<>();
        trialAppAccess.put("roles", Arrays.asList("case_worker", "CASE_WORKER", "Case_Worker"));
        resourceAccess.put("trial-app", trialAppAccess);

        Map<String, Object> claims = new HashMap<>();
        claims.put("resource_access", resourceAccess);
        claims.put("preferred_username", "testuser");

        Jwt jwt = createMockJwt(claims);

        // Extract authorities
        Collection<GrantedAuthority> authorities = jwtAuthenticationConverter
                .convert(jwt)
                .getAuthorities();

        // Verify roles are canonicalized and deduplicated
        long caseWorkerCount = authorities.stream()
                .filter(a -> a.getAuthority().equals("ROLE_CASE_WORKER"))
                .count();
        assertEquals(1, caseWorkerCount, "Should have exactly one CASE_WORKER role after canonicalization");
    }

    @Test
    void testEmptyJWTClaims() {
        // Create a JWT with minimal claims
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "user-123");

        Jwt jwt = createMockJwt(claims);

        // Extract authorities
        Collection<GrantedAuthority> authorities = jwtAuthenticationConverter
                .convert(jwt)
                .getAuthorities();

        // Should handle gracefully (may return empty or default role)
        assertNotNull(authorities, "Authorities should not be null");
    }

    @Test
    void testMultipleClientRoles() {
        // Test JWT with multiple client roles
        Map<String, Object> resourceAccess = new HashMap<>();
        Map<String, Object> trialAppAccess = new HashMap<>();
        trialAppAccess.put("roles", Arrays.asList("ADMIN", "SUPERVISOR", "CASE_WORKER"));
        resourceAccess.put("trial-app", trialAppAccess);

        Map<String, Object> claims = new HashMap<>();
        claims.put("resource_access", resourceAccess);
        claims.put("preferred_username", "testuser");

        Jwt jwt = createMockJwt(claims);

        // Extract authorities
        Collection<GrantedAuthority> authorities = jwtAuthenticationConverter
                .convert(jwt)
                .getAuthorities();

        // Verify all roles are extracted
        assertTrue(authorities.size() >= 1, "Should have at least one authority");
        Set<String> authorityNames = new HashSet<>();
        authorities.forEach(a -> authorityNames.add(a.getAuthority()));
        
        // Should contain at least one of the roles
        assertTrue(authorityNames.contains("ROLE_ADMIN") || 
                   authorityNames.contains("ROLE_SUPERVISOR") || 
                   authorityNames.contains("ROLE_CASE_WORKER"),
                "Should contain at least one of the client roles");
    }

    /**
     * Helper method to create a mock JWT for testing
     */
    private Jwt createMockJwt(Map<String, Object> claims) {
        return Jwt.withTokenValue("mock-token")
                .header("alg", "RS256")
                .claim("sub", claims.getOrDefault("sub", "test-user-id"))
                .claims(c -> c.putAll(claims))
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
    }
}

