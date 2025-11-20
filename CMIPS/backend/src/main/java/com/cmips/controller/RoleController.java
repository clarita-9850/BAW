package com.cmips.controller;

import com.cmips.entity.Role;
import com.cmips.repository.RoleRepository;
import com.cmips.service.PolicyEngineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/roles")
@CrossOrigin(origins = "*")
public class RoleController {
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private PolicyEngineService policyEngineService;
    
    // Get all roles
    @GetMapping
    public ResponseEntity<List<Role>> getAllRoles() {
        try {
            System.out.println("🔍 RoleController.getAllRoles() called");
            List<Role> roles = roleRepository.findAllByOrderByDisplayNameAsc();
            System.out.println("✅ Found " + roles.size() + " roles in database");
            return ResponseEntity.ok(roles);
        } catch (Exception e) {
            System.err.println("❌ Error fetching roles: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // Get active roles only
    @GetMapping("/active")
    public ResponseEntity<List<Role>> getActiveRoles() {
        try {
            List<Role> roles = roleRepository.findByActiveTrueOrderByDisplayNameAsc();
            System.out.println("✅ Found " + roles.size() + " active roles in database");
            return ResponseEntity.ok(roles);
        } catch (Exception e) {
            System.err.println("❌ Error fetching active roles: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // Get role by ID
    @GetMapping("/{id}")
    public ResponseEntity<Role> getRoleById(@PathVariable Long id) {
        try {
            Optional<Role> role = roleRepository.findById(id);
            if (role.isPresent()) {
                return ResponseEntity.ok(role.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // Create new role
    @PostMapping
    public ResponseEntity<Role> createRole(@RequestBody Role role) {
        try {
            // Check if role name already exists
            if (roleRepository.existsByName(role.getName())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }
            
            Role savedRole = roleRepository.save(role);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedRole);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // Update role
    @PutMapping("/{id}")
    public ResponseEntity<Role> updateRole(@PathVariable Long id, @RequestBody Role roleDetails) {
        try {
            Optional<Role> roleOptional = roleRepository.findById(id);
            if (!roleOptional.isPresent()) {
                return ResponseEntity.notFound().build();
            }
            
            Role role = roleOptional.get();
            
            // Check if new name conflicts with existing role (excluding current role)
            if (!role.getName().equals(roleDetails.getName()) && 
                roleRepository.existsByName(roleDetails.getName())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }
            
            role.setName(roleDetails.getName());
            role.setDisplayName(roleDetails.getDisplayName());
            role.setDescription(roleDetails.getDescription());
            role.setDepartment(roleDetails.getDepartment());
            role.setLocation(roleDetails.getLocation());
            role.setActive(roleDetails.getActive());
            
            Role updatedRole = roleRepository.save(role);
            return ResponseEntity.ok(updatedRole);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // Delete role
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRole(@PathVariable Long id) {
        try {
            Optional<Role> roleOptional = roleRepository.findById(id);
            if (!roleOptional.isPresent()) {
                return ResponseEntity.notFound().build();
            }
            
            // Check if role is being used by any users
            // TODO: Add check for users with this role before allowing deletion
            // For now, we'll allow deletion but this should be enhanced
            
            // Permanently delete the role from the database
            roleRepository.deleteById(id);
            
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // Activate/Deactivate role
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<Role> toggleRoleStatus(@PathVariable Long id) {
        try {
            Optional<Role> roleOptional = roleRepository.findById(id);
            if (!roleOptional.isPresent()) {
                return ResponseEntity.notFound().build();
            }
            
            Role role = roleOptional.get();
            role.setActive(!role.getActive());
            Role updatedRole = roleRepository.save(role);
            
            return ResponseEntity.ok(updatedRole);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
