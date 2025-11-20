package com.cmips.repository;

import com.cmips.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByUsername(String username);
    
    Optional<User> findByUsernameAndActiveTrue(String username);
    
    List<User> findByRole(String role);
    
    List<User> findByActiveTrue();
    
    @Query("SELECT u FROM User u WHERE u.role = :role AND u.active = true")
    List<User> findActiveUsersByRole(@Param("role") String role);
    
    @Query("SELECT u FROM User u WHERE u.department = :department AND u.active = true")
    List<User> findActiveUsersByDepartment(@Param("department") String department);
    
    @Query("SELECT u FROM User u WHERE u.location = :location AND u.active = true")
    List<User> findActiveUsersByLocation(@Param("location") String location);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
}




