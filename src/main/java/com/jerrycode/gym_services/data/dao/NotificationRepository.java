package com.jerrycode.gym_services.data.dao;


import com.jerrycode.gym_services.data.vo.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
}