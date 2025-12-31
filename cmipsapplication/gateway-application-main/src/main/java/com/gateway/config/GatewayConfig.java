package com.gateway.config;

import com.gateway.filter.AuthProxyFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    private final AuthProxyFilter authProxyFilter;

    public GatewayConfig(AuthProxyFilter authProxyFilter) {
        this.authProxyFilter = authProxyFilter;
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Auth endpoints - route to backend (AuthController handles Keycloak authentication)
                .route("auth-backend", r -> r
                        .path("/api/auth/**")
                        .filters(f -> f
                                .dedupeResponseHeader("Access-Control-Allow-Origin", "RETAIN_FIRST")
                                .dedupeResponseHeader("Access-Control-Allow-Credentials", "RETAIN_FIRST")
                        )
                        .uri("http://cmips-backend:8081"))
                
                // CMIPS backend routes - check before general /api/** to avoid conflicts
                // /api/cmips/** routes are for CMIPS-specific endpoints
                .route("cmips-backend", r -> r
                        .path("/api/cmips/**")
                        .filters(f -> f
                                .rewritePath("/api/cmips/(?<segment>.*)", "/api/${segment}")
                                .dedupeResponseHeader("Access-Control-Allow-Origin", "RETAIN_FIRST")
                                .dedupeResponseHeader("Access-Control-Allow-Credentials", "RETAIN_FIRST"))
                        .uri("http://cmips-backend:8081"))
                
                // Analytics, BI, Pipeline, Reports, Field-Masking - route to CMIPS backend (integrated IHSS features)
                .route("analytics", r -> r
                        .path("/api/analytics/**")
                        .filters(f -> f
                                .dedupeResponseHeader("Access-Control-Allow-Origin", "RETAIN_FIRST")
                                .dedupeResponseHeader("Access-Control-Allow-Credentials", "RETAIN_FIRST"))
                        .uri("http://cmips-backend:8081"))
                
                .route("bi", r -> r
                        .path("/api/bi/**")
                        .filters(f -> f
                                .dedupeResponseHeader("Access-Control-Allow-Origin", "RETAIN_FIRST")
                                .dedupeResponseHeader("Access-Control-Allow-Credentials", "RETAIN_FIRST"))
                        .uri("http://cmips-backend:8081"))
                
                .route("pipeline", r -> r
                        .path("/api/pipeline/**")
                        .filters(f -> f
                                .dedupeResponseHeader("Access-Control-Allow-Origin", "RETAIN_FIRST")
                                .dedupeResponseHeader("Access-Control-Allow-Credentials", "RETAIN_FIRST"))
                        .uri("http://cmips-backend:8081"))
                
                .route("reports", r -> r
                        .path("/api/reports/**")
                        .filters(f -> f
                                .dedupeResponseHeader("Access-Control-Allow-Origin", "RETAIN_FIRST")
                                .dedupeResponseHeader("Access-Control-Allow-Credentials", "RETAIN_FIRST"))
                        .uri("http://cmips-backend:8081"))
                
                .route("field-masking", r -> r
                        .path("/api/field-masking/**")
                        .filters(f -> f
                                .dedupeResponseHeader("Access-Control-Allow-Origin", "RETAIN_FIRST")
                                .dedupeResponseHeader("Access-Control-Allow-Credentials", "RETAIN_FIRST"))
                        .uri("http://cmips-backend:8081"))
                
                // Routes without /api/ prefix - legacy support for frontend-timesheets
                // Rewrite path to add /api/ prefix and route to CMIPS backend
                .route("analytics-legacy", r -> r
                        .path("/analytics/**")
                        .filters(f -> f
                                .rewritePath("/analytics/(?<segment>.*)", "/api/analytics/${segment}")
                                .dedupeResponseHeader("Access-Control-Allow-Origin", "RETAIN_FIRST")
                                .dedupeResponseHeader("Access-Control-Allow-Credentials", "RETAIN_FIRST"))
                        .uri("http://cmips-backend:8081"))
                
                .route("bi-legacy", r -> r
                        .path("/bi/**")
                        .filters(f -> f
                                .rewritePath("/bi/(?<segment>.*)", "/api/bi/${segment}")
                                .dedupeResponseHeader("Access-Control-Allow-Origin", "RETAIN_FIRST")
                                .dedupeResponseHeader("Access-Control-Allow-Credentials", "RETAIN_FIRST"))
                        .uri("http://cmips-backend:8081"))
                
                .route("pipeline-legacy", r -> r
                        .path("/pipeline/**")
                        .filters(f -> f
                                .rewritePath("/pipeline/(?<segment>.*)", "/api/pipeline/${segment}")
                                .dedupeResponseHeader("Access-Control-Allow-Origin", "RETAIN_FIRST")
                                .dedupeResponseHeader("Access-Control-Allow-Credentials", "RETAIN_FIRST"))
                        .uri("http://cmips-backend:8081"))
                
                .route("reports-legacy", r -> r
                        .path("/reports/**")
                        .filters(f -> f
                                .rewritePath("/reports/(?<segment>.*)", "/api/reports/${segment}")
                                .dedupeResponseHeader("Access-Control-Allow-Origin", "RETAIN_FIRST")
                                .dedupeResponseHeader("Access-Control-Allow-Credentials", "RETAIN_FIRST"))
                        .uri("http://cmips-backend:8081"))
                
                .route("field-masking-legacy", r -> r
                        .path("/field-masking/**")
                        .filters(f -> f
                                .rewritePath("/field-masking/(?<segment>.*)", "/api/field-masking/${segment}")
                                .dedupeResponseHeader("Access-Control-Allow-Origin", "RETAIN_FIRST")
                                .dedupeResponseHeader("Access-Control-Allow-Credentials", "RETAIN_FIRST"))
                        .uri("http://cmips-backend:8081"))
                
                // All other /api/** routes - route to CMIPS backend (main application)
                .route("cmips-api", r -> r
                        .path("/api/**")
                        .filters(f -> f
                                .dedupeResponseHeader("Access-Control-Allow-Origin", "RETAIN_FIRST")
                                .dedupeResponseHeader("Access-Control-Allow-Credentials", "RETAIN_FIRST"))
                        .uri("http://cmips-backend:8081"))
                
                .build();
    }
}

