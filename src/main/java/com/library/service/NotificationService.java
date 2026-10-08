package com.library.service;

import java.time.Clock;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.library.entity.Notification;
import com.library.entity.User;
import com.library.repository.NotificationRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

	private final NotificationRepository notificationRepository;
	private final Clock clock;

	public void notify(User user, String message) {
		Notification notification = new Notification();
		notification.setUser(user);
		notification.setMessage(message);
		notification.setCreatedAt(LocalDateTime.now(clock));
		notificationRepository.save(notification);

		log.info("Notification for {}: {}", user.getEmail(), message);
	}
}
