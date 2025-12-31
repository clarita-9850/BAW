package com.cmips.repository;

import com.cmips.entity.PersonNoteEntity;
import com.cmips.entity.PersonNoteEntity.PersonNoteType;
import com.cmips.entity.PersonNoteEntity.ContactMethod;
import com.cmips.entity.PersonNoteEntity.NoteCategory;
import com.cmips.entity.PersonNoteEntity.NoteImportance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository for PersonNoteEntity
 * Implements DSD Section 20 - Person Notes queries
 */
@Repository
public interface PersonNoteRepository extends JpaRepository<PersonNoteEntity, String> {

    // Find by person ID
    List<PersonNoteEntity> findByPersonIdOrderByNoteDateDescNoteTimeDesc(Long personId);

    // Find by person ID and type
    List<PersonNoteEntity> findByPersonIdAndPersonTypeOrderByNoteDateDescNoteTimeDesc(Long personId, PersonNoteType personType);

    // Find by referral ID
    List<PersonNoteEntity> findByReferralIdOrderByNoteDateDescNoteTimeDesc(String referralId);

    // Find by case ID
    List<PersonNoteEntity> findByCaseIdOrderByNoteDateDescNoteTimeDesc(Long caseId);

    // Find active notes by person
    @Query("SELECT n FROM PersonNoteEntity n WHERE n.personId = :personId AND n.active = true ORDER BY n.noteDate DESC, n.noteTime DESC")
    List<PersonNoteEntity> findActiveByPersonId(@Param("personId") Long personId);

    // Find by category
    List<PersonNoteEntity> findByPersonIdAndCategory(Long personId, NoteCategory category);

    // Find by contact method
    List<PersonNoteEntity> findByPersonIdAndContactMethod(Long personId, ContactMethod contactMethod);

    // Find by importance
    List<PersonNoteEntity> findByPersonIdAndImportance(Long personId, NoteImportance importance);

    // Find notes needing follow-up
    @Query("SELECT n FROM PersonNoteEntity n WHERE n.followUpNeeded = true AND n.followUpCompleted = false AND n.active = true")
    List<PersonNoteEntity> findNotesNeedingFollowUp();

    // Find notes needing follow-up by person
    @Query("SELECT n FROM PersonNoteEntity n WHERE n.personId = :personId AND n.followUpNeeded = true AND n.followUpCompleted = false AND n.active = true")
    List<PersonNoteEntity> findNotesNeedingFollowUpByPerson(@Param("personId") Long personId);

    // Find notes needing follow-up by date
    @Query("SELECT n FROM PersonNoteEntity n WHERE n.followUpDate <= :date AND n.followUpNeeded = true AND n.followUpCompleted = false AND n.active = true")
    List<PersonNoteEntity> findNotesNeedingFollowUpByDate(@Param("date") LocalDate date);

    // Find by date range
    @Query("SELECT n FROM PersonNoteEntity n WHERE n.personId = :personId AND n.noteDate BETWEEN :startDate AND :endDate ORDER BY n.noteDate DESC")
    List<PersonNoteEntity> findByPersonIdAndDateRange(
            @Param("personId") Long personId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // Find by created by (worker)
    List<PersonNoteEntity> findByCreatedBy(String createdBy);

    // Find critical/high importance notes
    @Query("SELECT n FROM PersonNoteEntity n WHERE n.personId = :personId AND (n.importance = 'CRITICAL' OR n.importance = 'HIGH') AND n.active = true ORDER BY n.noteDate DESC")
    List<PersonNoteEntity> findImportantNotes(@Param("personId") Long personId);

    // Find confidential notes
    @Query("SELECT n FROM PersonNoteEntity n WHERE n.personId = :personId AND n.confidential = true AND n.active = true ORDER BY n.noteDate DESC")
    List<PersonNoteEntity> findConfidentialNotes(@Param("personId") Long personId);

    // Find supervisor-only notes
    @Query("SELECT n FROM PersonNoteEntity n WHERE n.personId = :personId AND n.supervisorOnly = true AND n.active = true ORDER BY n.noteDate DESC")
    List<PersonNoteEntity> findSupervisorOnlyNotes(@Param("personId") Long personId);

    // Count notes by person
    Long countByPersonId(Long personId);

    // Count active notes by person
    @Query("SELECT COUNT(n) FROM PersonNoteEntity n WHERE n.personId = :personId AND n.active = true")
    Long countActiveByPersonId(@Param("personId") Long personId);

    // Search notes by content
    @Query("SELECT n FROM PersonNoteEntity n WHERE n.personId = :personId AND " +
           "(UPPER(n.subject) LIKE UPPER(CONCAT('%', :searchTerm, '%')) OR " +
           "UPPER(n.content) LIKE UPPER(CONCAT('%', :searchTerm, '%'))) AND n.active = true ORDER BY n.noteDate DESC")
    List<PersonNoteEntity> searchNotes(
            @Param("personId") Long personId,
            @Param("searchTerm") String searchTerm);

    // Find recent notes (last N days)
    @Query("SELECT n FROM PersonNoteEntity n WHERE n.personId = :personId AND n.noteDate >= :sinceDate AND n.active = true ORDER BY n.noteDate DESC")
    List<PersonNoteEntity> findRecentNotes(
            @Param("personId") Long personId,
            @Param("sinceDate") LocalDate sinceDate);

    // Find notes for referral that are editable
    @Query("SELECT n FROM PersonNoteEntity n WHERE n.referralId = :referralId AND n.editableUntil > CURRENT_TIMESTAMP AND n.active = true")
    List<PersonNoteEntity> findEditableNotesByReferral(@Param("referralId") String referralId);
}
