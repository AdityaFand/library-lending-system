package com.library.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.library.entity.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
}
