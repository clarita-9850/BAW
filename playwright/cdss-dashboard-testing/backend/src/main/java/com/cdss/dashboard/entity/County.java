package com.cdss.dashboard.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "counties")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class County {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "county_code", unique = true)
    private String countyCode;

    @Column(name = "county_name")
    private String countyName;

    @Column(name = "population")
    private Long population;

    @Column(name = "region")
    private String region;
}
