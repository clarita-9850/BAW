package com.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Global filter to validate JWT tokens and add user info to request headers
 * This runs after Spring Security validates the JWT
 */
@Component
public class JwtValidationFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // Skip auth endpoints and actuator - they don't need JWT validation
        String path = exchange.getRequest().getURI().getPath();
        if (path.startsWith("/api/auth/") || path.startsWith("/actuator/")) {
            return chain.filter(exchange);
        }

        // Get authentication from security context
        return ReactiveSecurityContextHolder.getContext()
            .cast(SecurityContext.class)
            .map(SecurityContext::getAuthentication)
            .flatMap(authentication -> {
                if (authentication != null && authentication.getPrincipal() instanceof Jwt) {
                    Jwt jwt = (Jwt) authentication.getPrincipal();
                    
                    // Extract roles from realm_access claim
                    String roles = "";
                    try {
                        java.util.Map<String, Object> realmAccess = jwt.getClaim("realm_access");
                        if (realmAccess != null && realmAccess.containsKey("roles")) {
                            @SuppressWarnings("unchecked")
                            java.util.List<String> roleList = (java.util.List<String>) realmAccess.get("roles");
                            roles = String.join(",", roleList);
                        }
                    } catch (Exception e) {
                        // If roles extraction fails, continue without roles header
                    }
                    
                    // Extract subject/user ID
                    String userId = jwt.getSubject() != null ? jwt.getSubject() : (jwt.getId() != null ? jwt.getId() : "");
                    
                    // Add user info to request headers for backend services
                    ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                        .header("X-User-Id", userId)
                        .header("X-User-Name", jwt.getClaimAsString("preferred_username") != null ? jwt.getClaimAsString("preferred_username") : "")
                        .header("X-User-Email", jwt.getClaimAsString("email") != null ? jwt.getClaimAsString("email") : "")
                        .header("X-User-Roles", roles)
                        .header("Authorization", "Bearer " + jwt.getTokenValue()) // Forward original token
                        .build();

                    return chain.filter(exchange.mutate().request(modifiedRequest).build());
                }
                // If no authentication but path is not protected, continue
                return chain.filter(exchange);
            })
            .switchIfEmpty(chain.filter(exchange));
    }

    @Override
    public int getOrder() {
        // Run after authentication filter but before routing
        // Lower number = higher priority
        return -100;
    }
}

