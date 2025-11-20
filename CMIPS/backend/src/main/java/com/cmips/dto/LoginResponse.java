package com.cmips.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    
    private String ssoToken;
    private String jwtToken;
    private String username;
    private String role;
    private String message;
    private Long expiresIn; // milliseconds
    
    public LoginResponse(String ssoToken, String jwtToken, String username, String role) {
        this.ssoToken = ssoToken;
        this.jwtToken = jwtToken;
        this.username = username;
        this.role = role;
        this.message = "Login successful";
        this.expiresIn = 86400000L; // 24 hours
    }
    
    public LoginResponse(String message, Long expiresIn) {
        this.message = message;
        this.expiresIn = expiresIn;
    }
}
