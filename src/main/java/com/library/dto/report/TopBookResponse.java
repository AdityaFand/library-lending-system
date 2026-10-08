package com.library.dto.report;

public record TopBookResponse(
		Long bookId,
		String title,
		String author,
		Long borrowCount) {
}
