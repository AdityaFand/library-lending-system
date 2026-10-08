package com.library.dto.auth;

import com.library.entity.User;
import com.library.enums.Role;

public record UserResponse(
		Long id,
		String name,
		String email,
		Role role) {

	public static UserResponse from(User user) {
		return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getRole());
	}
}
