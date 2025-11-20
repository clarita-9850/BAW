package com.cmips.service;

import com.cmips.entity.User;
import com.cmips.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class MockLdapService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    // Mock LDAP user database
    private final Map<String, String> mockLdapUsers = new HashMap<>();
    
    public MockLdapService() {
        // Initialize mock LDAP users
        mockLdapUsers.put("admin", "admin123");
        mockLdapUsers.put("caseworker", "case123");
        mockLdapUsers.put("auditor", "audit123");
        mockLdapUsers.put("manager", "manager123");
        mockLdapUsers.put("supervisor", "super123");
    }
    
    /**
     * Authenticate user against mock LDAP
     * @param username username
     * @param password password
     * @return SSO token if successful, null if failed
     */
    public String authenticateUser(String username, String password) {
        // Check mock LDAP
        if (mockLdapUsers.containsKey(username) && mockLdapUsers.get(username).equals(password)) {
            // Generate mock SSO token
            return generateSsoToken(username);
        }
        
        // Also check database users (for admin-created users)
        Optional<User> user = userRepository.findByUsernameAndActiveTrue(username);
        if (user.isPresent() && passwordEncoder.matches(password, user.get().getPasswordHash())) {
            return generateSsoToken(username);
        }
        
        return null;
    }
    
    /**
     * Generate mock SSO token
     * @param username username
     * @return SSO token
     */
    private String generateSsoToken(String username) {
        return "SSO-" + UUID.randomUUID().toString() + "-" + username;
    }
    
    /**
     * Validate SSO token
     * @param ssoToken SSO token
     * @return username if valid, null if invalid
     */
    public String validateSsoToken(String ssoToken) {
        if (ssoToken == null || !ssoToken.startsWith("SSO-")) {
            return null;
        }
        
        try {
            // Extract username from SSO token
            String[] parts = ssoToken.split("-");
            if (parts.length >= 3) {
                String username = parts[2];
                // Verify user exists
                if (mockLdapUsers.containsKey(username) || userRepository.findByUsername(username).isPresent()) {
                    return username;
                }
            }
        } catch (Exception e) {
            // Invalid token format
        }
        
        return null;
    }
    
    /**
     * Get user details from mock LDAP
     * @param username username
     * @return user details or null if not found
     */
    public Map<String, String> getUserDetails(String username) {
        Map<String, String> details = new HashMap<>();
        
        // Check if user exists in mock LDAP
        if (mockLdapUsers.containsKey(username)) {
            details.put("username", username);
            details.put("email", username + "@cmips.com");
            details.put("firstName", getFirstName(username));
            details.put("lastName", getLastName(username));
            details.put("department", getDepartment(username));
            details.put("location", getLocation(username));
            return details;
        }
        
        // Check database
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isPresent()) {
            User u = user.get();
            details.put("username", u.getUsername());
            details.put("email", u.getEmail());
            details.put("firstName", u.getFirstName());
            details.put("lastName", u.getLastName());
            details.put("department", u.getDepartment());
            details.put("location", u.getLocation());
            return details;
        }
        
        return null;
    }
    
    private String getFirstName(String username) {
        switch (username) {
            case "admin": return "Admin";
            case "caseworker": return "John";
            case "auditor": return "Jane";
            case "manager": return "Mike";
            case "supervisor": return "Sarah";
            default: return "User";
        }
    }
    
    private String getLastName(String username) {
        switch (username) {
            case "admin": return "User";
            case "caseworker": return "Doe";
            case "auditor": return "Smith";
            case "manager": return "Johnson";
            case "supervisor": return "Wilson";
            default: return "Name";
        }
    }
    
    private String getDepartment(String username) {
        switch (username) {
            case "admin": return "IT";
            case "caseworker": return "Case Management";
            case "auditor": return "Audit";
            case "manager": return "Management";
            case "supervisor": return "Supervision";
            default: return "General";
        }
    }
    
    private String getLocation(String username) {
        switch (username) {
            case "admin": return "Headquarters";
            case "caseworker": return "County Office A";
            case "auditor": return "County Office B";
            case "manager": return "Regional Office";
            case "supervisor": return "County Office A";
            default: return "Unknown";
        }
    }
}
