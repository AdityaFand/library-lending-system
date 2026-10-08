package com.library.dto.member;

import com.library.entity.User;

public record MemberResponse(
		Long id,
		String name,
		String email,
		long openLoans,
		long overdueLoans) {

	public static MemberResponse from(User user, long openLoans, long overdueLoans) {
		return new MemberResponse(user.getId(), user.getName(), user.getEmail(), openLoans, overdueLoans);
	}
}
