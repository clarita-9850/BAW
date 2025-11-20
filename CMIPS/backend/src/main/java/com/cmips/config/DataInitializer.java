package com.cmips.config;

import com.cmips.entity.Policy;
import com.cmips.entity.Role;
import com.cmips.entity.User;
import com.cmips.repository.PolicyRepository;
import com.cmips.repository.RoleRepository;
import com.cmips.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private PolicyRepository policyRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) throws Exception {
        initializeRoles();
        initializeUsers();
        // initializePolicies(); // Disabled - let user create policies manually
    }
    
    private void initializeRoles() {
        // Check if roles already exist
        if (roleRepository.count() > 0) {
            System.out.println("✅ Roles already exist, skipping initialization");
            return;
        }
        
        // Create default CMIPS roles
        List<Role> roles = Arrays.asList(
            new Role("ADMIN", "Administrator", "Full system administrator with access to all features", "IT", "Headquarters"),
            new Role("CASE_WORKER", "Case Worker", "Handles case management and client services", "Case Management", "County Office"),
            new Role("AUDITOR", "Auditor", "Reviews and audits system data and processes", "Audit", "Regional Office"),
            new Role("MANAGER", "Manager", "Manages team operations and has supervisory access", "Management", "Regional Office"),
            new Role("SUPERVISOR", "Supervisor", "Supervises case workers and manages daily operations", "Supervision", "County Office"),
            new Role("SOCIAL_WORKER", "Social Worker", "Provides social services and case support", "Social Services", "County Office"),
            new Role("FINANCE_CLERK", "Finance Clerk", "Handles financial transactions and payment processing", "Finance", "County Office"),
            new Role("DATA_ENTRY", "Data Entry Clerk", "Enters and maintains client and case data", "Data Management", "County Office"),
            new Role("QUALITY_ASSURANCE", "Quality Assurance Specialist", "Ensures data quality and compliance", "Quality Assurance", "Regional Office"),
            new Role("TRAINING_COORDINATOR", "Training Coordinator", "Coordinates staff training and development", "Human Resources", "Regional Office")
        );
        
        roleRepository.saveAll(roles);
        System.out.println("✅ Initialized " + roles.size() + " roles");
    }
    
    private void initializeUsers() {
        // Check if users already exist
        if (userRepository.count() > 0) {
            return;
        }
        
        // Create default users
        List<User> users = Arrays.asList(
            new User("admin", passwordEncoder.encode("admin123"), "ADMIN", "admin@cmips.com", "Admin", "User", "IT", "Headquarters"),
            new User("caseworker", passwordEncoder.encode("case123"), "CASE_WORKER", "caseworker@cmips.com", "John", "Doe", "Case Management", "County Office A"),
            new User("auditor", passwordEncoder.encode("audit123"), "AUDITOR", "auditor@cmips.com", "Jane", "Smith", "Audit", "County Office B"),
            new User("manager", passwordEncoder.encode("manager123"), "MANAGER", "manager@cmips.com", "Mike", "Johnson", "Management", "Regional Office"),
            new User("supervisor", passwordEncoder.encode("super123"), "SUPERVISOR", "supervisor@cmips.com", "Sarah", "Wilson", "Supervision", "County Office A")
        );
        
        userRepository.saveAll(users);
        System.out.println("✅ Initialized " + users.size() + " users");
    }
    
    private void initializePolicies() {
        // Always initialize policies for debugging - clear existing ones
        policyRepository.deleteAll();
        System.out.println("🔄 Clearing existing policies and reinitializing...");
        
        // Create default policies
        List<Policy> policies = Arrays.asList(
            // Admin policies - full access
            new Policy("ADMIN", "*", "*", true, "Admin has full access to all resources", 100),
            
            // Admin role management policies
            new Policy("ADMIN", "roles", "GET", true, "Admin can view roles", 10),
            new Policy("ADMIN", "roles", "POST", true, "Admin can create roles", 10),
            new Policy("ADMIN", "roles", "PUT", true, "Admin can update roles", 10),
            new Policy("ADMIN", "roles", "DELETE", true, "Admin can delete roles", 10),
            new Policy("ADMIN", "roles", "PATCH", true, "Admin can modify roles", 10),
            
            // Admin policy management policies
            new Policy("ADMIN", "policies", "GET", true, "Admin can view policies", 10),
            new Policy("ADMIN", "policies", "POST", true, "Admin can create policies", 10),
            new Policy("ADMIN", "policies", "PUT", true, "Admin can update policies", 10),
            new Policy("ADMIN", "policies", "DELETE", true, "Admin can delete policies", 10),
            new Policy("ADMIN", "policies", "PATCH", true, "Admin can modify policies", 10),
            
            // Case Worker policies - access to all screens
            new Policy("CASE_WORKER", "timesheets", "GET", true, "Case workers can view timesheets", 10),
            new Policy("CASE_WORKER", "timesheets", "POST", true, "Case workers can create timesheets", 10),
            new Policy("CASE_WORKER", "timesheets", "PUT", true, "Case workers can update their own timesheets", 10),
            new Policy("CASE_WORKER", "timesheets", "DELETE", true, "Case workers can delete their own timesheets", 10),
            new Policy("CASE_WORKER", "cases", "GET", true, "Case workers can view cases", 10),
            new Policy("CASE_WORKER", "cases", "POST", true, "Case workers can create cases", 10),
            new Policy("CASE_WORKER", "cases", "PUT", true, "Case workers can update cases", 10),
            new Policy("CASE_WORKER", "cases", "DELETE", true, "Case workers can delete cases", 10),
            new Policy("CASE_WORKER", "person_search", "GET", true, "Case workers can search persons", 10),
            new Policy("CASE_WORKER", "person_search", "POST", true, "Case workers can create person records", 10),
            new Policy("CASE_WORKER", "person_search", "PUT", true, "Case workers can update person records", 10),
            new Policy("CASE_WORKER", "person_search", "DELETE", true, "Case workers can delete person records", 10),
            new Policy("CASE_WORKER", "payments", "GET", true, "Case workers can view payments", 10),
            new Policy("CASE_WORKER", "payments", "POST", true, "Case workers can create payments", 10),
            new Policy("CASE_WORKER", "payments", "PUT", true, "Case workers can update payments", 10),
            new Policy("CASE_WORKER", "payments", "DELETE", true, "Case workers can delete payments", 10),
            new Policy("CASE_WORKER", "users", "GET", false, "Case workers cannot view user management", 10),
            new Policy("CASE_WORKER", "policies", "GET", false, "Case workers cannot view policies", 10),
            
            // Auditor policies - read-only access
            new Policy("AUDITOR", "timesheets", "GET", true, "Auditors can view all timesheets", 10),
            new Policy("AUDITOR", "timesheets", "POST", false, "Auditors cannot create timesheets", 10),
            new Policy("AUDITOR", "timesheets", "PUT", false, "Auditors cannot update timesheets", 10),
            new Policy("AUDITOR", "timesheets", "DELETE", false, "Auditors cannot delete timesheets", 10),
            new Policy("AUDITOR", "cases", "GET", true, "Auditors can view cases", 10),
            new Policy("AUDITOR", "cases", "POST", false, "Auditors cannot create cases", 10),
            new Policy("AUDITOR", "cases", "PUT", false, "Auditors cannot update cases", 10),
            new Policy("AUDITOR", "cases", "DELETE", false, "Auditors cannot delete cases", 10),
            new Policy("AUDITOR", "person_search", "GET", true, "Auditors can search persons", 10),
            new Policy("AUDITOR", "person_search", "POST", false, "Auditors cannot create person records", 10),
            new Policy("AUDITOR", "person_search", "PUT", false, "Auditors cannot update person records", 10),
            new Policy("AUDITOR", "person_search", "DELETE", false, "Auditors cannot delete person records", 10),
            new Policy("AUDITOR", "payments", "GET", true, "Auditors can view payments", 10),
            new Policy("AUDITOR", "payments", "POST", false, "Auditors cannot create payments", 10),
            new Policy("AUDITOR", "payments", "PUT", false, "Auditors cannot update payments", 10),
            new Policy("AUDITOR", "payments", "DELETE", false, "Auditors cannot delete payments", 10),
            new Policy("AUDITOR", "users", "GET", false, "Auditors cannot view user management", 10),
            new Policy("AUDITOR", "policies", "GET", false, "Auditors cannot view policies", 10),
            
            // Manager policies - full access to screens, limited admin access
            new Policy("MANAGER", "timesheets", "GET", true, "Managers can view all timesheets", 10),
            new Policy("MANAGER", "timesheets", "POST", true, "Managers can create timesheets", 10),
            new Policy("MANAGER", "timesheets", "PUT", true, "Managers can update timesheets", 10),
            new Policy("MANAGER", "timesheets", "DELETE", true, "Managers can delete timesheets", 10),
            new Policy("MANAGER", "cases", "GET", true, "Managers can view cases", 10),
            new Policy("MANAGER", "cases", "POST", true, "Managers can create cases", 10),
            new Policy("MANAGER", "cases", "PUT", true, "Managers can update cases", 10),
            new Policy("MANAGER", "cases", "DELETE", true, "Managers can delete cases", 10),
            new Policy("MANAGER", "person_search", "GET", true, "Managers can search persons", 10),
            new Policy("MANAGER", "person_search", "POST", true, "Managers can create person records", 10),
            new Policy("MANAGER", "person_search", "PUT", true, "Managers can update person records", 10),
            new Policy("MANAGER", "person_search", "DELETE", true, "Managers can delete person records", 10),
            new Policy("MANAGER", "payments", "GET", true, "Managers can view payments", 10),
            new Policy("MANAGER", "payments", "POST", true, "Managers can create payments", 10),
            new Policy("MANAGER", "payments", "PUT", true, "Managers can update payments", 10),
            new Policy("MANAGER", "payments", "DELETE", true, "Managers can delete payments", 10),
            new Policy("MANAGER", "users", "GET", true, "Managers can view users", 10),
            new Policy("MANAGER", "policies", "GET", false, "Managers cannot view policies", 10),
            
            // Supervisor policies - full access to screens
            new Policy("SUPERVISOR", "timesheets", "GET", true, "Supervisors can view timesheets", 10),
            new Policy("SUPERVISOR", "timesheets", "POST", true, "Supervisors can create timesheets", 10),
            new Policy("SUPERVISOR", "timesheets", "PUT", true, "Supervisors can update timesheets", 10),
            new Policy("SUPERVISOR", "timesheets", "DELETE", true, "Supervisors can delete timesheets", 10),
            new Policy("SUPERVISOR", "cases", "GET", true, "Supervisors can view cases", 10),
            new Policy("SUPERVISOR", "cases", "POST", true, "Supervisors can create cases", 10),
            new Policy("SUPERVISOR", "cases", "PUT", true, "Supervisors can update cases", 10),
            new Policy("SUPERVISOR", "cases", "DELETE", true, "Supervisors can delete cases", 10),
            new Policy("SUPERVISOR", "person_search", "GET", true, "Supervisors can search persons", 10),
            new Policy("SUPERVISOR", "person_search", "POST", true, "Supervisors can create person records", 10),
            new Policy("SUPERVISOR", "person_search", "PUT", true, "Supervisors can update person records", 10),
            new Policy("SUPERVISOR", "person_search", "DELETE", true, "Supervisors can delete person records", 10),
            new Policy("SUPERVISOR", "payments", "GET", true, "Supervisors can view payments", 10),
            new Policy("SUPERVISOR", "payments", "POST", true, "Supervisors can create payments", 10),
            new Policy("SUPERVISOR", "payments", "PUT", true, "Supervisors can update payments", 10),
            new Policy("SUPERVISOR", "payments", "DELETE", true, "Supervisors can delete payments", 10),
            new Policy("SUPERVISOR", "users", "GET", false, "Supervisors cannot view user management", 10),
            new Policy("SUPERVISOR", "policies", "GET", false, "Supervisors cannot view policies", 10),
            
            // Default deny policy
            new Policy("*", "*", "*", false, "Default deny all access", 0)
        );
        
        policyRepository.saveAll(policies);
        System.out.println("✅ Initialized " + policies.size() + " policies");
    }
}


