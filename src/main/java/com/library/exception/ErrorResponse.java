package com.library.exception;

import java.time.LocalDateTime;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
		LocalDateTime timestamp,
		int status,
		String error,
		String message,
		Map<String, String> fieldErrors) {

	public static ErrorResponse of(int status, String error, String message) {
		return new ErrorResponse(LocalDateTime.now(), status, error, message, null);
	}
}
