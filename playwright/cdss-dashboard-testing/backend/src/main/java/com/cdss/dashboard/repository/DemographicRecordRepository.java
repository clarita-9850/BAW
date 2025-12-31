package com.cdss.dashboard.repository;

import com.cdss.dashboard.entity.DemographicRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DemographicRecordRepository extends JpaRepository<DemographicRecord, Long> {

    List<DemographicRecord> findByCountyCode(String countyCode);

    List<DemographicRecord> findByEthnicity(String ethnicity);

    List<DemographicRecord> findByGender(String gender);

    List<DemographicRecord> findByAgeGroup(String ageGroup);

    List<DemographicRecord> findByAgedBlindDisabled(String agedBlindDisabled);

    List<DemographicRecord> findByCaseType(String caseType);

    List<DemographicRecord> findBySeverelyImpaired(Boolean severelyImpaired);

    @Query("SELECT DISTINCT d.countyCode FROM DemographicRecord d")
    List<String> findDistinctCountyCodes();

    @Query("SELECT DISTINCT d.ethnicity FROM DemographicRecord d WHERE d.ethnicity IS NOT NULL")
    List<String> findDistinctEthnicities();

    @Query("SELECT DISTINCT d.gender FROM DemographicRecord d WHERE d.gender IS NOT NULL")
    List<String> findDistinctGenders();

    @Query("SELECT DISTINCT d.ageGroup FROM DemographicRecord d WHERE d.ageGroup IS NOT NULL")
    List<String> findDistinctAgeGroups();

    @Query("SELECT DISTINCT d.race FROM DemographicRecord d WHERE d.race IS NOT NULL")
    List<String> findDistinctRaces();

    @Query("SELECT DISTINCT d.agedBlindDisabled FROM DemographicRecord d WHERE d.agedBlindDisabled IS NOT NULL")
    List<String> findDistinctAgedBlindDisabled();

    @Query("SELECT DISTINCT d.caseType FROM DemographicRecord d WHERE d.caseType IS NOT NULL")
    List<String> findDistinctCaseTypes();

    @Query("SELECT SUM(d.individualCount) FROM DemographicRecord d")
    Long getTotalIndividuals();

    @Query("SELECT SUM(d.countyPopulation) FROM DemographicRecord d")
    Long getTotalPopulation();

    @Query("SELECT SUM(d.authorizedHours) FROM DemographicRecord d")
    Double getTotalAuthorizedHours();

    @Query("SELECT d FROM DemographicRecord d WHERE " +
           "(:countyCode IS NULL OR d.countyCode = :countyCode) AND " +
           "(:ethnicity IS NULL OR d.ethnicity = :ethnicity) AND " +
           "(:gender IS NULL OR d.gender = :gender) AND " +
           "(:ageGroup IS NULL OR d.ageGroup = :ageGroup) AND " +
           "(:race IS NULL OR d.race = :race) AND " +
           "(:agedBlindDisabled IS NULL OR d.agedBlindDisabled = :agedBlindDisabled) AND " +
           "(:caseType IS NULL OR d.caseType = :caseType) AND " +
           "(:severelyImpaired IS NULL OR d.severelyImpaired = :severelyImpaired)")
    List<DemographicRecord> findByFilters(
            @Param("countyCode") String countyCode,
            @Param("ethnicity") String ethnicity,
            @Param("gender") String gender,
            @Param("ageGroup") String ageGroup,
            @Param("race") String race,
            @Param("agedBlindDisabled") String agedBlindDisabled,
            @Param("caseType") String caseType,
            @Param("severelyImpaired") Boolean severelyImpaired
    );
}
