package com.cmips.controller;

import com.cmips.dto.LoginRequest;
import com.cmips.dto.LoginResponse;
import com.cmips.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    
    @Autowired
    private AuthService authService;
    
    /**
     * Login endpoint - authenticate with mock LDAP and get SSO token
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            // Directly authenticate and generate JWT
            LoginResponse response = authService.authenticateAndGenerateJwt(loginRequest);
            
            if (response == null) {
                return ResponseEntity.status(401)
                    .body(new LoginResponse("Invalid credentials", 0L));
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(new LoginResponse("Login failed: " + e.getMessage(), 0L));
        }
    }
    
    /**
     * SSO token exchange endpoint - exchange SSO token for JWT
     * POST /api/auth/sso/exchange
     */
    @PostMapping("/sso/exchange")
    public ResponseEntity<LoginResponse> exchangeSsoToken(@RequestParam("ssoToken") String ssoToken) {
        try {
            LoginResponse response = authService.exchangeSsoForJwt(ssoToken);
            
            if (response == null) {
                return ResponseEntity.badRequest()
                    .body(new LoginResponse("Invalid SSO token", 0L));
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(new LoginResponse("Token exchange failed: " + e.getMessage(), 0L));
        }
    }
    
    /**
     * Health check endpoint
     * GET /api/auth/health
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Auth service is running");
    }
}
