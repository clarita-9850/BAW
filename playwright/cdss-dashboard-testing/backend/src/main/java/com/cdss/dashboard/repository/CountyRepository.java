package com.cdss.dashboard.repository;

import com.cdss.dashboard.entity.County;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CountyRepository extends JpaRepository<County, Long> {

    Optional<County> findByCountyCode(String countyCode);

    Optional<County> findByCountyName(String countyName);
}
