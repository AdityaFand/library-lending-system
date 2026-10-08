package com.library.dto.me;

import java.time.LocalDateTime;

import com.library.entity.Notification;

public record NotificationResponse(
		Long id,
		String message,
		LocalDateTime createdAt,
		boolean read) {

	public static NotificationResponse from(Notification notification) {
		return new NotificationResponse(notification.getId(), notification.getMessage(),
				notification.getCreatedAt(), notification.isRead());
	}
}
