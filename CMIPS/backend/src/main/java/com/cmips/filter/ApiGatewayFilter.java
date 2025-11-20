package com.cmips.filter;

import com.cmips.entity.User;
import com.cmips.service.AuthService;
import com.cmips.service.PolicyEngineService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.regex.Pattern;

@Component
public class ApiGatewayFilter extends OncePerRequestFilter {
    
    @Autowired
    private AuthService authService;
    
    @Autowired
    private PolicyEngineService policyEngineService;
    
    // Public endpoints that don't require authentication
    private static final String[] PUBLIC_ENDPOINTS = {
        "/api/auth/login",
        "/api/auth/sso/exchange",
        "/api/test",
        "/actuator/health",
        "/actuator/info"
    };
    
    // Admin endpoints that bypass policy checking
    private static final String[] ADMIN_BYPASS_ENDPOINTS = {
        "/api/roles",
        "/api/policies",
        "/api/admin"
    };
    
    // Admin endpoints that require ADMIN role
    private static final String[] ADMIN_ENDPOINTS = {
        "/api/admin/.*",
        "/api/policies/.*",
        "/api/roles/.*"
    };
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        String requestPath = request.getRequestURI();
        String method = request.getMethod();
        
        // Skip authentication for public endpoints
        if (isPublicEndpoint(requestPath)) {
            filterChain.doFilter(request, response);
            return;
        }
        
        // Extract JWT token from Authorization header
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            sendErrorResponse(response, "Missing or invalid Authorization header", HttpStatus.UNAUTHORIZED);
            return;
        }
        
        String jwtToken = authHeader.substring(7);
        
        // Validate JWT token
        User user = authService.validateJwtToken(jwtToken);
        if (user == null) {
            sendErrorResponse(response, "Invalid or expired JWT token", HttpStatus.UNAUTHORIZED);
            return;
        }
        
        // Get user role from JWT
        String role = authService.getRoleFromJwt(jwtToken);
        if (role == null) {
            sendErrorResponse(response, "Unable to extract role from JWT token", HttpStatus.UNAUTHORIZED);
            return;
        }
        
        // Check if this is an admin bypass endpoint
        boolean isBypass = isAdminBypassEndpoint(requestPath);
        System.out.println("🔍 Request path: " + requestPath + ", isBypass: " + isBypass + ", role: " + role);
        
        if (isBypass) {
            // For admin bypass endpoints, just check if user is ADMIN
            if (!"ADMIN".equals(role)) {
                sendErrorResponse(response, "Access denied: Admin role required", HttpStatus.FORBIDDEN);
                return;
            }
            System.out.println("✅ Admin bypass endpoint, allowing access");
        } else {
            // Extract resource and action from request
            String resource = extractResource(requestPath);
            String action = method;
            
            System.out.println("🔍 Policy check - Role: " + role + ", Resource: " + resource + ", Action: " + action);
            
            // Check authorization using policy engine
            boolean isAllowed = policyEngineService.isAllowed(role, resource, action);
            System.out.println("🔍 Policy result: " + isAllowed);
            
            if (!isAllowed) {
                sendErrorResponse(response, "Access denied: Insufficient permissions for " + action + " on " + resource, HttpStatus.FORBIDDEN);
                return;
            }
        }
        
        // Add user info to request attributes for use in controllers
        request.setAttribute("userRole", role);
        request.setAttribute("jwtToken", jwtToken);
        
        // Continue to the next filter/controller
        filterChain.doFilter(request, response);
    }
    
    /**
     * Check if the request path is a public endpoint
     * @param path request path
     * @return true if public endpoint
     */
    private boolean isPublicEndpoint(String path) {
        for (String endpoint : PUBLIC_ENDPOINTS) {
            if (path.equals(endpoint)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Check if the request path is an admin bypass endpoint
     * @param path request path
     * @return true if admin bypass endpoint
     */
    private boolean isAdminBypassEndpoint(String path) {
        for (String endpoint : ADMIN_BYPASS_ENDPOINTS) {
            if (path.startsWith(endpoint)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Extract resource name from request path
     * @param path request path
     * @return resource name
     */
    private String extractResource(String path) {
        // Remove /api prefix
        if (path.startsWith("/api/")) {
            path = path.substring(5);
        }
        
        // Extract first part as resource
        String[] parts = path.split("/");
        if (parts.length > 0) {
            return parts[0];
        }
        
        return "unknown";
    }
    
    /**
     * Send error response
     * @param response HTTP response
     * @param message error message
     * @param status HTTP status
     * @throws IOException if writing response fails
     */
    private void sendErrorResponse(HttpServletResponse response, String message, HttpStatus status) throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        String jsonResponse = String.format(
            "{\"error\": \"%s\", \"message\": \"%s\", \"status\": %d}",
            status.getReasonPhrase(),
            message,
            status.value()
        );
        
        response.getWriter().write(jsonResponse);
    }
}
