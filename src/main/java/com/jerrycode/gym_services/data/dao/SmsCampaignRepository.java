package com.jerrycode.gym_services.data.dao;


import com.jerrycode.gym_services.data.vo.SmsCampaign;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SmsCampaignRepository extends JpaRepository<SmsCampaign, Long> {
}