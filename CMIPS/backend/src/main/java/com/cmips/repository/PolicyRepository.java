package com.cmips.repository;

import com.cmips.entity.Policy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PolicyRepository extends JpaRepository<Policy, Long> {
    
    List<Policy> findByRoleAndActiveTrue(String role);
    
    List<Policy> findByResourceAndActiveTrue(String resource);
    
    List<Policy> findByRoleAndResourceAndActiveTrue(String role, String resource);
    
    List<Policy> findByRoleAndResourceAndActionAndActiveTrue(String role, String resource, String action);
    
    @Query("SELECT p FROM Policy p WHERE p.role = :role AND p.resource = :resource AND p.action = :action AND p.active = true ORDER BY p.priority DESC")
    List<Policy> findPoliciesForAccess(@Param("role") String role, @Param("resource") String resource, @Param("action") String action);
    
    @Query("SELECT p FROM Policy p WHERE p.active = true ORDER BY p.role, p.resource, p.action")
    List<Policy> findAllActivePolicies();
    
    @Query("SELECT p FROM Policy p WHERE p.role = :role AND p.active = true ORDER BY p.priority DESC")
    List<Policy> findActivePoliciesByRole(@Param("role") String role);
    
    @Query("SELECT p FROM Policy p WHERE p.resource = :resource AND p.active = true ORDER BY p.role, p.priority DESC")
    List<Policy> findActivePoliciesByResource(@Param("resource") String resource);
    
    boolean existsByRoleAndResourceAndActionAndActiveTrue(String role, String resource, String action);
}




