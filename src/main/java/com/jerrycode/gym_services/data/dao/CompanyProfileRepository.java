package com.jerrycode.gym_services.data.dao;

import com.jerrycode.gym_services.data.vo.CompanyProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompanyProfileRepository extends JpaRepository<CompanyProfile, Long> {
    @Query("SELECT c FROM CompanyProfile c ORDER BY c.id ASC")
    Optional<CompanyProfile> findFirst();

    Optional<CompanyProfile> findTopByOrderByIdAsc();
}