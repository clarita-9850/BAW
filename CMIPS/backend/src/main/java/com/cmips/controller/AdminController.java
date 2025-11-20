package com.cmips.controller;

import com.cmips.entity.User;
import com.cmips.entity.Role;
import com.cmips.repository.UserRepository;
import com.cmips.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.HashMap;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    /**
     * Get all users
     * GET /api/admin/users
     */
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers(HttpServletRequest httpRequest) {
        try {
            // Check if user has admin privileges
            String role = (String) httpRequest.getAttribute("userRole");
            if (!"ADMIN".equals(role)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            List<User> users = userRepository.findByActiveTrue();
            return ResponseEntity.ok(users);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get user by ID
     * GET /api/admin/users/{id}
     */
    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id, HttpServletRequest httpRequest) {
        try {
            // Check if user has admin privileges
            String role = (String) httpRequest.getAttribute("userRole");
            if (!"ADMIN".equals(role)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            Optional<User> user = userRepository.findById(id);
            if (user.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(user.get());
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get users by role
     * GET /api/admin/users/role/{role}
     */
    @GetMapping("/users/role/{role}")
    public ResponseEntity<List<User>> getUsersByRole(@PathVariable String role, 
                                                   HttpServletRequest httpRequest) {
        try {
            // Check if user has admin privileges
            String userRole = (String) httpRequest.getAttribute("userRole");
            if (!"ADMIN".equals(userRole)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            List<User> users = userRepository.findActiveUsersByRole(role);
            return ResponseEntity.ok(users);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Update user role
     * PUT /api/admin/users/{id}/role
     */
    @PutMapping("/users/{id}/role")
    public ResponseEntity<User> updateUserRole(@PathVariable Long id, 
                                             @RequestParam String role,
                                             HttpServletRequest httpRequest) {
        try {
            // Check if user has admin privileges
            String userRole = (String) httpRequest.getAttribute("userRole");
            if (!"ADMIN".equals(userRole)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            Optional<User> user = userRepository.findById(id);
            if (user.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            User existingUser = user.get();
            existingUser.setRole(role);
            
            User updatedUser = userRepository.save(existingUser);
            return ResponseEntity.ok(updatedUser);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Deactivate user
     * PUT /api/admin/users/{id}/deactivate
     */
    @PutMapping("/users/{id}/deactivate")
    public ResponseEntity<User> deactivateUser(@PathVariable Long id, HttpServletRequest httpRequest) {
        try {
            // Check if user has admin privileges
            String role = (String) httpRequest.getAttribute("userRole");
            if (!"ADMIN".equals(role)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            Optional<User> user = userRepository.findById(id);
            if (user.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            User existingUser = user.get();
            existingUser.setActive(false);
            
            User updatedUser = userRepository.save(existingUser);
            return ResponseEntity.ok(updatedUser);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Activate user
     * PUT /api/admin/users/{id}/activate
     */
    @PutMapping("/users/{id}/activate")
    public ResponseEntity<User> activateUser(@PathVariable Long id, HttpServletRequest httpRequest) {
        try {
            // Check if user has admin privileges
            String role = (String) httpRequest.getAttribute("userRole");
            if (!"ADMIN".equals(role)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            Optional<User> user = userRepository.findById(id);
            if (user.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            User existingUser = user.get();
            existingUser.setActive(true);
            
            User updatedUser = userRepository.save(existingUser);
            return ResponseEntity.ok(updatedUser);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get system statistics
     * GET /api/admin/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<Object> getSystemStats(HttpServletRequest httpRequest) {
        try {
            // Check if user has admin privileges
            String role = (String) httpRequest.getAttribute("userRole");
            if (!"ADMIN".equals(role)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            long totalRoles = roleRepository.count();
            long activeRoles = roleRepository.findByActiveTrueOrderByDisplayNameAsc().size();
            long inactiveRoles = totalRoles - activeRoles;
            
            Map<String, Long> stats = new HashMap<>();
            stats.put("totalRoles", totalRoles);
            stats.put("activeRoles", activeRoles);
            stats.put("inactiveRoles", inactiveRoles);
            
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
