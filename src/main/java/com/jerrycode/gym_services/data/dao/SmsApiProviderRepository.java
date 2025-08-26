package com.jerrycode.gym_services.data.dao;

import com.jerrycode.gym_services.data.vo.SmsApiProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SmsApiProviderRepository extends JpaRepository<SmsApiProvider,Long> {
    Optional<SmsApiProvider> findByProviderName(String smsPackageProviderName);
    Optional<SmsApiProvider> findTopByOrderByIdAsc();
}
