package com.cdss.dashboard.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "demographic_records")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DemographicRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "county_code")
    private String countyCode;

    @Column(name = "county_name")
    private String countyName;

    @Column(name = "race")
    private String race;

    @Column(name = "ethnicity")
    private String ethnicity;

    @Column(name = "gender")
    private String gender;

    @Column(name = "age_group")
    private String ageGroup;

    @Column(name = "aged_blind_disabled")
    private String agedBlindDisabled;

    @Column(name = "severely_impaired")
    private Boolean severelyImpaired;

    @Column(name = "evv_status")
    private String evvStatus;

    @Column(name = "case_type")
    private String caseType; // PM or PS

    @Column(name = "individual_count")
    private Long individualCount;

    @Column(name = "county_population")
    private Long countyPopulation;

    @Column(name = "authorized_hours")
    private Double authorizedHours;

    @Column(name = "per_capita_rate")
    private Double perCapitaRate;

    @Column(name = "applications_received")
    private Long applicationsReceived;

    @Column(name = "applications_approved")
    private Long applicationsApproved;

    @Column(name = "applications_denied")
    private Long applicationsDenied;

    @Column(name = "active_error_rate")
    private Double activeErrorRate;

    @Column(name = "federal_persons")
    private Long federalPersons;

    @Column(name = "non_assistance_hours")
    private Double nonAssistanceHours;

    @Column(name = "non_assistance_persons")
    private Long nonAssistancePersons;

    @Column(name = "public_assistance_hours")
    private Double publicAssistanceHours;

    @Column(name = "public_assistance_persons")
    private Long publicAssistancePersons;
}
