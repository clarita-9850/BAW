package com.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Global filter to override X-Forwarded-Proto header for Keycloak routes
 * Runs AFTER Gateway's XForwarded filter (order 0) to ensure our header takes precedence
 * This ensures Keycloak with sslRequired=external accepts requests
 */
@Component
public class KeycloakHttpsHeaderFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        
        // Only process Keycloak auth routes
        if (path.startsWith("/api/auth/")) {
            System.out.println("KeycloakHttpsHeaderFilter: Processing /api/auth/ request");
            System.out.println("KeycloakHttpsHeaderFilter: Original X-Forwarded-Proto = " + request.getHeaders().getFirst("X-Forwarded-Proto"));
            
            // Create a new request with X-Forwarded-Proto set to https
            // This overrides Gateway's automatic X-Forwarded-Proto header
            // Also remove Forwarded header which Keycloak might check
            ServerHttpRequestDecorator decoratedRequest = new ServerHttpRequestDecorator(request) {
                @Override
                public HttpHeaders getHeaders() {
                    HttpHeaders headers = new HttpHeaders();
                    headers.putAll(super.getHeaders());
                    // Remove any existing proxy headers
                    headers.remove("X-Forwarded-Proto");
                    headers.remove("Forwarded");
                    // Set X-Forwarded-Proto to https so Keycloak accepts the request
                    headers.set("X-Forwarded-Proto", "https");
                    System.out.println("KeycloakHttpsHeaderFilter: Modified X-Forwarded-Proto = " + headers.getFirst("X-Forwarded-Proto"));
                    return headers;
                }
            };
            
            return chain.filter(exchange.mutate().request(decoratedRequest).build());
        }
        
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        // Run AFTER Gateway's XForwarded filter (order 0) but BEFORE NettyRoutingFilter
        // Higher order number = runs later
        // NettyRoutingFilter runs at 2147483647, so order 10000 should be safe
        return 10000;
    }
}

