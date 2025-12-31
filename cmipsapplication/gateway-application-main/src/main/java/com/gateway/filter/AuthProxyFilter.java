package com.gateway.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Filter to proxy authentication requests to Keycloak
 * Transforms /api/auth/* requests to Keycloak's /realms/cmips/protocol/openid-connect/token
 * Handles request body transformation from JSON to form-urlencoded
 */
@Component
public class AuthProxyFilter extends AbstractGatewayFilterFactory<AuthProxyFilter.Config> {

    private static final String REALM = "cmips";
    private static final String CLIENT_ID = "trial-app";
    private static final String CLIENT_SECRET = "trial-app-secret-key-2024";
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AuthProxyFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getURI().getPath();

            // Only process auth endpoints
            if (path.startsWith("/api/auth/")) {
                // Read request body
                return DataBufferUtils.join(request.getBody())
                    .defaultIfEmpty(exchange.getResponse().bufferFactory().allocateBuffer(0))
                    .flatMap(dataBuffer -> {
                        try {
                            if (dataBuffer.readableByteCount() == 0) {
                                // Empty body - return error or pass through
                                return chain.filter(exchange);
                            }
                            
                            byte[] bytes = new byte[dataBuffer.readableByteCount()];
                            dataBuffer.read(bytes);
                            DataBufferUtils.release(dataBuffer);
                            
                            String body = new String(bytes, StandardCharsets.UTF_8);
                            if (body.trim().isEmpty()) {
                                return chain.filter(exchange);
                            }
                            
                            String transformedBody = transformRequestBody(body, path);
                            if (transformedBody == null) {
                                // Transformation failed - return error
                                exchange.getResponse().setStatusCode(org.springframework.http.HttpStatus.BAD_REQUEST);
                                return exchange.getResponse().setComplete();
                            }
                            
                            // Create new request with transformed body
                            // Transform path to Keycloak token endpoint
                            String keycloakPath = "/realms/" + REALM + "/protocol/openid-connect/token";
                            URI keycloakUri = URI.create("http://cmips-keycloak:8080" + keycloakPath);
                            
                            // Create request with transformed body
                            // Remove existing proxy headers and set X-Forwarded-Proto: https so Keycloak accepts the request
                            ServerHttpRequest modifiedRequest = request.mutate()
                                .uri(keycloakUri)
                                .headers(headers -> {
                                    headers.remove("X-Forwarded-Proto");
                                    headers.remove("Forwarded");
                                    headers.set("X-Forwarded-Proto", "https");
                                    headers.set(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded");
                                    headers.set(HttpHeaders.CONTENT_LENGTH, String.valueOf(transformedBody.length()));
                                })
                                .build();
                            
                            // Create request decorator with transformed body
                            // Use exchange's buffer factory
                            DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(transformedBody.getBytes(StandardCharsets.UTF_8));
                            ServerHttpRequestDecorator decorator = new ServerHttpRequestDecorator(modifiedRequest) {
                                @Override
                                public Flux<DataBuffer> getBody() {
                                    return Flux.just(buffer);
                                }
                            };
                            
                            return chain.filter(exchange.mutate().request(decorator).build());
                        } catch (Exception e) {
                            System.err.println("Error in AuthProxyFilter: " + e.getMessage());
                            e.printStackTrace();
                            exchange.getResponse().setStatusCode(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR);
                            return exchange.getResponse().setComplete();
                        }
                    })
                    .onErrorResume(e -> {
                        System.err.println("Error processing auth request: " + e.getMessage());
                        e.printStackTrace();
                        exchange.getResponse().setStatusCode(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR);
                        return exchange.getResponse().setComplete();
                    })
                    .switchIfEmpty(chain.filter(exchange));
            }

            return chain.filter(exchange);
        };
    }

    private String transformRequestBody(String body, String path) {
        try {
            if (path.contains("/login")) {
                // Parse JSON and convert to form-urlencoded
                @SuppressWarnings("unchecked")
                Map<String, Object> jsonMap = objectMapper.readValue(body, Map.class);
                String username = String.valueOf(jsonMap.getOrDefault("username", ""));
                String password = String.valueOf(jsonMap.getOrDefault("password", ""));
                
                if (username.isEmpty() || password.isEmpty()) {
                    System.err.println("Missing username or password in login request");
                    return null;
                }
                
                return String.format(
                    "grant_type=password&client_id=%s&client_secret=%s&username=%s&password=%s",
                    CLIENT_ID, CLIENT_SECRET, 
                    java.net.URLEncoder.encode(username, StandardCharsets.UTF_8),
                    java.net.URLEncoder.encode(password, StandardCharsets.UTF_8)
                );
            } else if (path.contains("/refresh")) {
                @SuppressWarnings("unchecked")
                Map<String, Object> jsonMap = objectMapper.readValue(body, Map.class);
                String refreshToken = String.valueOf(jsonMap.getOrDefault("refresh_token", ""));
                
                if (refreshToken.isEmpty()) {
                    System.err.println("Missing refresh_token in refresh request");
                    return null;
                }
                
                return String.format(
                    "grant_type=refresh_token&client_id=%s&client_secret=%s&refresh_token=%s",
                    CLIENT_ID, CLIENT_SECRET, 
                    java.net.URLEncoder.encode(refreshToken, StandardCharsets.UTF_8)
                );
            }
        } catch (Exception e) {
            // Log error and return null to indicate failure
            System.err.println("Error transforming request body: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
        
        return null; // Return null if path doesn't match
    }

    public static class Config {
        // Configuration properties if needed
    }
}

