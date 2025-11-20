package com.cmips.service;

import com.cmips.entity.Policy;
import com.cmips.repository.PolicyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PolicyEngineService {
    
    @Autowired
    private PolicyRepository policyRepository;
    
    /**
     * Check if a role is allowed to perform an action on a resource
     * @param role user role
     * @param resource resource name
     * @param action action (GET, POST, PUT, DELETE)
     * @return true if allowed, false if denied
     */
    @Cacheable(value = "policies", key = "#role + '_' + #resource + '_' + #action")
    public boolean isAllowed(String role, String resource, String action) {
        // Get all policies for this role, resource, and action
        List<Policy> policies = policyRepository.findPoliciesForAccess(role, resource, action);
        
        if (policies.isEmpty()) {
            // No specific policy found, check for wildcard policies
            return checkWildcardPolicies(role, resource, action);
        }
        
        // Get the highest priority policy (first in the list due to ORDER BY priority DESC)
        Policy policy = policies.get(0);
        return policy.getAllowed();
    }
    
    /**
     * Check wildcard policies when no specific policy is found
     * @param role user role
     * @param resource resource name
     * @param action action
     * @return true if allowed by wildcard, false otherwise
     */
    private boolean checkWildcardPolicies(String role, String resource, String action) {
        // Check for wildcard resource policy
        List<Policy> wildcardResourcePolicies = policyRepository.findByRoleAndResourceAndActionAndActiveTrue(role, "*", action);
        if (!wildcardResourcePolicies.isEmpty()) {
            return wildcardResourcePolicies.get(0).getAllowed();
        }
        
        // Check for wildcard action policy
        List<Policy> wildcardActionPolicies = policyRepository.findByRoleAndResourceAndActionAndActiveTrue(role, resource, "*");
        if (!wildcardActionPolicies.isEmpty()) {
            return wildcardActionPolicies.get(0).getAllowed();
        }
        
        // Check for wildcard resource and action policy
        List<Policy> wildcardAllPolicies = policyRepository.findByRoleAndResourceAndActionAndActiveTrue(role, "*", "*");
        if (!wildcardAllPolicies.isEmpty()) {
            return wildcardAllPolicies.get(0).getAllowed();
        }
        
        // Default deny if no policy found
        return false;
    }
    
    /**
     * Get all policies for a role
     * @param role user role
     * @return list of policies
     */
    public List<Policy> getPoliciesByRole(String role) {
        return policyRepository.findActivePoliciesByRole(role);
    }
    
    /**
     * Get all policies for a resource
     * @param resource resource name
     * @return list of policies
     */
    public List<Policy> getPoliciesByResource(String resource) {
        return policyRepository.findActivePoliciesByResource(resource);
    }
    
    /**
     * Get all active policies
     * @return list of all active policies
     */
    public List<Policy> getAllPolicies() {
        return policyRepository.findAllActivePolicies();
    }
    
    /**
     * Create a new policy
     * @param policy policy to create
     * @return created policy
     */
    public Policy createPolicy(Policy policy) {
        return policyRepository.save(policy);
    }
    
    /**
     * Update an existing policy
     * @param policy policy to update
     * @return updated policy
     */
    public Policy updatePolicy(Policy policy) {
        return policyRepository.save(policy);
    }
    
    /**
     * Delete a policy
     * @param id policy id
     */
    public void deletePolicy(Long id) {
        policyRepository.deleteById(id);
    }
    
    /**
     * Get policy by id
     * @param id policy id
     * @return policy if found
     */
    public Optional<Policy> getPolicyById(Long id) {
        return policyRepository.findById(id);
    }
    
    /**
     * Check if a policy exists for the given role, resource, and action
     * @param role user role
     * @param resource resource name
     * @param action action
     * @return true if policy exists
     */
    public boolean policyExists(String role, String resource, String action) {
        return policyRepository.existsByRoleAndResourceAndActionAndActiveTrue(role, resource, action);
    }
    
    /**
     * Clear policy cache (useful when policies are updated)
     */
    public void clearPolicyCache() {
        // This would be implemented with a cache manager in a real application
        // For now, we'll rely on the @Cacheable annotation's TTL
    }
}




