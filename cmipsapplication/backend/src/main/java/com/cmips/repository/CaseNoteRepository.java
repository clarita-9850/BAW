package com.cmips.repository;

import com.cmips.entity.CaseNoteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CaseNoteRepository extends JpaRepository<CaseNoteEntity, Long> {

    // Find by case (chronological order, most recent first)
    @Query("SELECT cn FROM CaseNoteEntity cn WHERE cn.caseId = :caseId ORDER BY cn.createdAt DESC")
    List<CaseNoteEntity> findByCaseIdOrderByCreatedAtDesc(@Param("caseId") Long caseId);

    // Find by recipient
    @Query("SELECT cn FROM CaseNoteEntity cn WHERE cn.recipientId = :recipientId ORDER BY cn.createdAt DESC")
    List<CaseNoteEntity> findByRecipientIdOrderByCreatedAtDesc(@Param("recipientId") Long recipientId);

    // Find by provider
    @Query("SELECT cn FROM CaseNoteEntity cn WHERE cn.providerId = :providerId ORDER BY cn.createdAt DESC")
    List<CaseNoteEntity> findByProviderIdOrderByCreatedAtDesc(@Param("providerId") Long providerId);

    // Find active notes by case
    @Query("SELECT cn FROM CaseNoteEntity cn WHERE cn.caseId = :caseId AND cn.status = 'ACTIVE' ORDER BY cn.createdAt DESC")
    List<CaseNoteEntity> findActiveNotesByCaseId(@Param("caseId") Long caseId);

    // Find by note type
    List<CaseNoteEntity> findByNoteType(String noteType);

    // Find by case and note type
    List<CaseNoteEntity> findByCaseIdAndNoteType(Long caseId, String noteType);

    // Search notes by content
    @Query("SELECT cn FROM CaseNoteEntity cn WHERE " +
           "(cn.caseId = :caseId OR :caseId IS NULL) AND " +
           "(LOWER(cn.content) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(cn.subject) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<CaseNoteEntity> searchNotes(@Param("caseId") Long caseId, @Param("searchTerm") String searchTerm);
}
