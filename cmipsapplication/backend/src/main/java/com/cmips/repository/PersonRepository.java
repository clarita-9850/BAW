package com.cmips.repository;

import com.cmips.entity.PersonEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PersonRepository extends JpaRepository<PersonEntity, Long> {
    
    /**
     * Search persons by first name and last name (case-insensitive, partial match)
     */
    List<PersonEntity> findByFirstNameContainingIgnoreCaseAndLastNameContainingIgnoreCase(
            String firstName, String lastName);
    
    /**
     * Search persons by first name only (case-insensitive, partial match)
     */
    List<PersonEntity> findByFirstNameContainingIgnoreCase(String firstName);
    
    /**
     * Search persons by last name only (case-insensitive, partial match)
     */
    List<PersonEntity> findByLastNameContainingIgnoreCase(String lastName);
    
    /**
     * Find person by SSN (exact match)
     * Note: SSN should be stored in format XXX-XX-XXXX
     */
    Optional<PersonEntity> findBySsn(String ssn);
    
    /**
     * Find person by first name, last name, and date of birth (exact match)
     * Used for duplicate checking
     */
    Optional<PersonEntity> findByFirstNameAndLastNameAndDateOfBirth(
            String firstName, String lastName, LocalDate dateOfBirth);
    
    /**
     * Search persons by county of residence
     */
    List<PersonEntity> findByCountyOfResidence(String countyOfResidence);
    
    /**
     * Custom query for flexible name search
     * Searches both first and last name fields
     */
    @Query("SELECT p FROM PersonEntity p WHERE " +
           "LOWER(p.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(CONCAT(p.firstName, ' ', p.lastName)) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<PersonEntity> searchByName(@Param("searchTerm") String searchTerm);
    
    /**
     * Search persons by SSN (partial match, last 4 digits)
     * Note: This searches for SSN ending with the provided digits
     */
    @Query("SELECT p FROM PersonEntity p WHERE p.ssn LIKE %:lastDigits")
    List<PersonEntity> findBySsnEndingWith(@Param("lastDigits") String lastDigits);
    
    /**
     * Check if person exists by SSN
     */
    boolean existsBySsn(String ssn);
    
    /**
     * Find persons created by a specific user
     */
    List<PersonEntity> findByCreatedBy(String createdBy);
}






