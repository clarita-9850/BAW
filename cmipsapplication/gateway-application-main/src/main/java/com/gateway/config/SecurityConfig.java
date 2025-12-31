package com.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
            .csrf(csrf -> csrf.disable())
            // Disable Spring Security CORS completely - Gateway's global CORS handles it
            // This prevents duplicate CORS headers from Spring Security + Gateway
            .cors(cors -> cors.disable())
            .authorizeExchange(exchanges -> exchanges
                // CRITICAL: Permit ALL OPTIONS requests FIRST (before any other rules)
                // Gateway's global CORS will handle adding CORS headers for OPTIONS preflight
                .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                // Public auth endpoints - no authentication required
                .pathMatchers("/api/auth/**").permitAll()
                // Actuator endpoints - public for health checks
                .pathMatchers("/actuator/**").permitAll()
                // All other endpoints (including legacy routes without /api/ prefix) require authentication
                .anyExchange().authenticated()
            )
            .httpBasic(httpBasic -> httpBasic.disable())
            .formLogin(formLogin -> formLogin.disable())
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtDecoder(jwtDecoder()))
            );

        return http.build();
    }

    @Bean
    public org.springframework.security.oauth2.jwt.ReactiveJwtDecoder jwtDecoder() {
        return org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder
                .withIssuerLocation("http://cmips-keycloak:8080/realms/cmips")
                .build();
    }
}

