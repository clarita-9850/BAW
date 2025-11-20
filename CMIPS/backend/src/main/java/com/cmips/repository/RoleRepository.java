package com.cmips.repository;

import com.cmips.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    
    // Find by role name
    Optional<Role> findByName(String name);
    
    // Find all active roles
    List<Role> findByActiveTrueOrderByDisplayNameAsc();
    
    // Find all roles ordered by display name
    List<Role> findAllByOrderByDisplayNameAsc();
    
    // Check if role name exists
    boolean existsByName(String name);
    
    // Find roles by department
    List<Role> findByDepartmentOrderByDisplayNameAsc(String department);
    
    // Find roles by location
    List<Role> findByLocationOrderByDisplayNameAsc(String location);
}


