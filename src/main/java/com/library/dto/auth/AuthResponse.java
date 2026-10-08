package com.library.dto.auth;

import com.library.enums.Role;

public record AuthResponse(
		String token,
		String tokenType,
		long expiresInMs,
		Long userId,
		String name,
		String email,
		Role role) {
}
