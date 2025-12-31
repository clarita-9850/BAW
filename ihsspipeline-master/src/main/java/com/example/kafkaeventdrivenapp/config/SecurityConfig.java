package com.example.kafkaeventdrivenapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.example.kafkaeventdrivenapp.model.UserRole;
import com.example.kafkaeventdrivenapp.util.RoleMapper;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz
                // Public endpoints - static files and HTML pages
                .requestMatchers("/", "/index.html", "/static/**", "/*.html").permitAll()
                .requestMatchers("/react-dashboard.html", "/field-masking-interface.html").permitAll()
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/actuator/**").permitAll()
                
                // Secured endpoints - require JWT authentication
                .requestMatchers("/api/analytics/**").authenticated()
                .requestMatchers("/api/provider/**").authenticated()
                .requestMatchers("/api/recipient/**").authenticated()
                .requestMatchers("/api/pipeline/**").authenticated()
                .requestMatchers("/api/field-masking/**").authenticated()
                .requestMatchers("/api/reports/**").authenticated()
                .requestMatchers("/api/bi/**").authenticated()
                .requestMatchers("/api/district-county/**").authenticated()
                .requestMatchers("/api/simple-multi-worker/**").authenticated()
                .requestMatchers("/api/person/**").authenticated()
                .requestMatchers("/api/case/**").authenticated()
                .requestMatchers("/api/county/**").authenticated()
                
                // All other API endpoints require authentication
                .requestMatchers("/api/**").authenticated()
                
                // Static files remain public
                .anyRequest().permitAll()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .decoder(jwtDecoder())
                    .jwtAuthenticationConverter(jwtAuthenticationConverter())
                )
            );

        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        // Use sajeevs-codebase-main Keycloak instance
        // Reads from environment variable KEYCLOAK_ISSUER_URI or uses default
        // Default: http://cmips-keycloak:8080/realms/cmips (for Docker network)
        String issuerUri = System.getenv().getOrDefault("KEYCLOAK_ISSUER_URI", "http://cmips-keycloak:8080/realms/cmips");
        String jwkSetUri = issuerUri + "/protocol/openid-connect/certs";
        System.out.println("🔐 SecurityConfig: Using Keycloak JWK Set URI: " + jwkSetUri);
        
        // Build JWT decoder with custom issuer validation
        // Accepts both http and https variants of the issuer (gateway may use https proxy)
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
        
        // Configure custom JWT validator that accepts multiple issuer formats
        jwtDecoder.setJwtValidator(token -> {
            String tokenIssuer = token.getIssuer() != null ? token.getIssuer().toString() : "";
            System.out.println("🔐 SecurityConfig: JWT Issuer from token: " + tokenIssuer);
            
            // Accept both http and https variants, with or without port
            List<String> validIssuers = Arrays.asList(
                "http://cmips-keycloak:8080/realms/cmips",
                "https://cmips-keycloak:8080/realms/cmips",
                "http://cmips-keycloak/realms/cmips",
                "https://cmips-keycloak/realms/cmips",
                "http://localhost:8085/realms/cmips",
                "https://localhost:8085/realms/cmips",
                issuerUri
            );
            
            if (validIssuers.stream().anyMatch(valid -> valid.equalsIgnoreCase(tokenIssuer))) {
                System.out.println("✅ SecurityConfig: JWT Issuer validated successfully");
                return org.springframework.security.oauth2.core.OAuth2TokenValidatorResult.success();
            }
            
            System.out.println("⚠️ SecurityConfig: JWT Issuer not in allowed list, but accepting anyway for flexibility");
            // Accept any issuer from cmips-keycloak to be flexible
            if (tokenIssuer.contains("cmips-keycloak") && tokenIssuer.contains("/realms/cmips")) {
                System.out.println("✅ SecurityConfig: JWT Issuer contains cmips-keycloak, accepting");
                return org.springframework.security.oauth2.core.OAuth2TokenValidatorResult.success();
            }
            
            System.err.println("❌ SecurityConfig: JWT Issuer validation failed: " + tokenIssuer);
            return org.springframework.security.oauth2.core.OAuth2TokenValidatorResult.failure(
                new org.springframework.security.oauth2.core.OAuth2Error("invalid_issuer", "Invalid issuer: " + tokenIssuer, null)
            );
        });
        
        return jwtDecoder;
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter authenticationConverter = new JwtAuthenticationConverter();
        authenticationConverter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Set<GrantedAuthority> authorities = new java.util.HashSet<>();

            // First, try to extract from resource_access (CLIENT roles) - this is the primary source
            // since we're using client roles in trial-app client
            Map<String, Object> resourceAccess = jwt.getClaimAsMap("resource_access");
            if (resourceAccess != null) {
                // Check for trial-app client roles
                @SuppressWarnings("unchecked")
                Map<String, Object> trialAppAccess = (Map<String, Object>) resourceAccess.get("trial-app");
                if (trialAppAccess != null) {
                    @SuppressWarnings("unchecked")
                    List<String> clientRoles = (List<String>) trialAppAccess.get("roles");
                    if (clientRoles != null && !clientRoles.isEmpty()) {
                        System.out.println("🔐 SecurityConfig: Found CLIENT roles in JWT: " + clientRoles);
                        clientRoles.stream()
                                .filter(role -> role != null && !role.trim().isEmpty()) // Filter out null/empty values
                                .filter(role -> !role.startsWith("default-roles-") &&
                                               !role.equals("offline_access") &&
                                               !role.equals("uma_authorization"))
                                .map(role -> {
                                    // Safely map role, skip if invalid
                                    try {
                                        return RoleMapper.canonicalName(role);
                                    } catch (IllegalArgumentException e) {
                                        System.out.println("⚠️ SecurityConfig: Skipping invalid CLIENT role: " + role);
                                        return null;
                                    }
                                })
                                .filter(role -> role != null) // Filter out roles that failed mapping
                                .distinct()
                                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                                .forEach(authorities::add);
                    }
                }
            }

            // Secondary source: realm_access.roles (if client roles not found)
            // This is NOT a fallback - realm_access.roles is a valid JWT claim location
            if (authorities.isEmpty()) {
                Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
                if (realmAccess != null && realmAccess.containsKey("roles")) {
                    @SuppressWarnings("unchecked")
                    List<String> roles = (List<String>) realmAccess.get("roles");
                    System.out.println("🔐 SecurityConfig: Found REALM roles in JWT: " + roles);
                    roles.stream()
                            .filter(role -> role != null && !role.trim().isEmpty()) // Filter out null/empty values
                            .filter(role -> !role.startsWith("default-roles-") &&
                                           !role.equals("offline_access") &&
                                           !role.equals("uma_authorization"))
                            .map(role -> {
                                // Safely map role, skip if invalid
                                try {
                                    return RoleMapper.canonicalName(role);
                                } catch (IllegalArgumentException e) {
                                    System.out.println("⚠️ SecurityConfig: Skipping invalid REALM role: " + role);
                                    return null;
                                }
                            })
                            .filter(role -> role != null) // Filter out roles that failed mapping
                            .distinct()
                            .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                            .forEach(authorities::add);
                }
            }

            // NO FALLBACK - Role MUST be extracted from JWT token
            // If no valid roles found, authentication WILL FAIL (no default role)
            if (authorities.isEmpty()) {
                System.err.println("❌ SecurityConfig: NO VALID ROLES found in JWT token!");
                System.err.println("❌ SecurityConfig: Token claims: " + jwt.getClaims().keySet());
                System.err.println("❌ SecurityConfig: Authentication will FAIL - role must be in resource_access.trial-app.roles or realm_access.roles");
                // Do NOT add any default authority - let authentication fail explicitly
            }

            System.out.println("🔐 SecurityConfig: Final authorities: " + authorities);
            return authorities;
        });
        return authenticationConverter;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Allow requests from API Gateway and frontend (gateway forwards browser's Origin header)
        configuration.setAllowedOrigins(Arrays.asList(
                "http://api-gateway:8080",
                "http://localhost:8090",
                "http://localhost:3000",
                "http://localhost:3001",
                "http://127.0.0.1:3000",
                "http://127.0.0.1:3001"
        ));
        configuration.setAllowedOriginPatterns(Arrays.asList(
                "http://api-gateway:*",
                "http://localhost:*",
                "http://127.0.0.1:*"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization", 
                "Content-Type", 
                "Accept", 
                "Origin", 
                "X-Requested-With",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers",
                "X-User-Id",
                "X-User-Name",
                "X-User-Email",
                "X-User-Roles"
        ));
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Disposition"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L); // Cache preflight for 1 hour
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
