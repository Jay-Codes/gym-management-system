package com.jerrycode.gym_services.data.dao;

import com.jerrycode.gym_services.data.vo.CompanyNotification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyNotificationRepository extends JpaRepository<CompanyNotification,Long> {
}
