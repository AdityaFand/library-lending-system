package com.library.dto.book;

import com.library.entity.BookCopy;
import com.library.enums.CopyStatus;

public record CopyResponse(
		Long id,
		Long bookId,
		CopyStatus status) {

	public static CopyResponse from(BookCopy copy) {
		return new CopyResponse(copy.getId(), copy.getBook().getId(), copy.getStatus());
	}
}
