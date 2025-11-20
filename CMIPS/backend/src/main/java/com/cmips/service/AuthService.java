package com.cmips.service;

import com.cmips.dto.LoginRequest;
import com.cmips.dto.LoginResponse;
import com.cmips.entity.User;
import com.cmips.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
public class AuthService {
    
    @Autowired
    private MockLdapService mockLdapService;
    
    @Autowired
    private JwtService jwtService;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Authenticate user and return SSO token
     * @param loginRequest login credentials
     * @return SSO token if successful, null if failed
     */
    public String authenticateUser(LoginRequest loginRequest) {
        return mockLdapService.authenticateUser(loginRequest.getUsername(), loginRequest.getPassword());
    }
    
    /**
     * Directly authenticate user and generate JWT (bypass SSO for simplicity)
     * @param loginRequest login credentials
     * @return LoginResponse with JWT token if successful, null if failed
     */
    public LoginResponse authenticateAndGenerateJwt(LoginRequest loginRequest) {
        // Check if user exists in database and password matches
        Optional<User> userOptional = userRepository.findByUsernameAndActiveTrue(loginRequest.getUsername());
        
        if (userOptional.isEmpty()) {
            return null;
        }
        
        User user = userOptional.get();
        
        // Check password (in real scenario, this would be hashed)
        // For now, we'll use the mock LDAP service to validate
        String ssoToken = mockLdapService.authenticateUser(loginRequest.getUsername(), loginRequest.getPassword());
        if (ssoToken == null) {
            return null;
        }
        
        // Generate JWT token
        String jwtToken = jwtService.generateToken(user.getUsername(), user.getRole());
        
        return new LoginResponse(ssoToken, jwtToken, user.getUsername(), user.getRole());
    }
    
    /**
     * Exchange SSO token for JWT token
     * @param ssoToken SSO token
     * @return JWT token if successful, null if failed
     */
    public LoginResponse exchangeSsoForJwt(String ssoToken) {
        // Validate SSO token
        String username = mockLdapService.validateSsoToken(ssoToken);
        if (username == null) {
            return null;
        }
        
        // Get user details
        User user = getUserByUsername(username);
        if (user == null) {
            return null;
        }
        
        // Generate JWT token
        String jwtToken = jwtService.generateToken(username, user.getRole());
        
        return new LoginResponse(ssoToken, jwtToken, username, user.getRole());
    }
    
    /**
     * Get user by username
     * @param username username
     * @return user if found, null otherwise
     */
    private User getUserByUsername(String username) {
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isPresent()) {
            return user.get();
        }
        
        // If user doesn't exist in database, create a default user based on mock LDAP
        Map<String, String> userDetails = mockLdapService.getUserDetails(username);
        if (userDetails != null) {
            User newUser = createDefaultUser(username, userDetails);
            return userRepository.save(newUser);
        }
        
        return null;
    }
    
    /**
     * Create a default user based on mock LDAP details
     * @param username username
     * @param userDetails user details from LDAP
     * @return new user
     */
    private User createDefaultUser(String username, Map<String, String> userDetails) {
        String role = getDefaultRole(username);
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash("ldap-authenticated"); // Password is handled by LDAP
        user.setRole(role);
        user.setEmail(userDetails.get("email"));
        user.setFirstName(userDetails.get("firstName"));
        user.setLastName(userDetails.get("lastName"));
        user.setDepartment(userDetails.get("department"));
        user.setLocation(userDetails.get("location"));
        user.setActive(true);
        return user;
    }
    
    /**
     * Get default role based on username
     * @param username username
     * @return default role
     */
    private String getDefaultRole(String username) {
        switch (username) {
            case "admin": return "ADMIN";
            case "caseworker": return "CASE_WORKER";
            case "auditor": return "AUDITOR";
            case "manager": return "MANAGER";
            case "supervisor": return "SUPERVISOR";
            default: return "USER";
        }
    }
    
    /**
     * Validate JWT token and get user details
     * @param jwtToken JWT token
     * @return user if valid, null if invalid
     */
    public User validateJwtToken(String jwtToken) {
        if (!jwtService.validateToken(jwtToken)) {
            return null;
        }
        
        String username = jwtService.extractUsername(jwtToken);
        return getUserByUsername(username);
    }
    
    /**
     * Get user role from JWT token
     * @param jwtToken JWT token
     * @return role if valid, null if invalid
     */
    public String getRoleFromJwt(String jwtToken) {
        if (!jwtService.validateToken(jwtToken)) {
            return null;
        }
        
        return jwtService.extractRole(jwtToken);
    }
}
