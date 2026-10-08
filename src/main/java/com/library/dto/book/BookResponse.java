package com.library.dto.book;

import java.math.BigDecimal;

import com.library.entity.Book;

public record BookResponse(
		Long id,
		String title,
		String isbn,
		String author,
		String category,
		BigDecimal price,
		long totalCopies,
		long availableCopies) {

	public static BookResponse from(Book book, long totalCopies, long availableCopies) {
		return new BookResponse(book.getId(), book.getTitle(), book.getIsbn(), book.getAuthor(),
				book.getCategory(), book.getPrice(), totalCopies, availableCopies);
	}
}
