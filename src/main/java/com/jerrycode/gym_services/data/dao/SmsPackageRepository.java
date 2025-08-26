package com.jerrycode.gym_services.data.dao;

import com.jerrycode.gym_services.data.vo.SmsPackage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SmsPackageRepository extends JpaRepository<SmsPackage,Long> {
}
