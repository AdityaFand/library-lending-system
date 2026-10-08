package com.library.specification;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.library.entity.Book;
import com.library.entity.BookCopy;
import com.library.enums.CopyStatus;

import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

public final class BookSpecifications {

	private BookSpecifications() {
	}

	public static Specification<Book> search(String title, String author, String category, Boolean available) {
		List<Specification<Book>> specs = new ArrayList<>();

		if (hasText(title)) {
			specs.add(fieldContains("title", title));
		}
		if (hasText(author)) {
			specs.add(fieldContains("author", author));
		}
		if (hasText(category)) {
			specs.add(categoryEquals(category));
		}
		if (available != null) {
			specs.add(hasAvailableCopy(available));
		}

		return Specification.allOf(specs);
	}

	private static Specification<Book> fieldContains(String field, String value) {
		String pattern = "%" + escapeLike(value.trim().toLowerCase()) + "%";
		return (root, query, cb) -> cb.like(cb.lower(root.get(field)), pattern, '\\');
	}

	private static Specification<Book> categoryEquals(String category) {
		String value = category.trim().toLowerCase();
		return (root, query, cb) -> cb.equal(cb.lower(root.get("category")), value);
	}

	private static Specification<Book> hasAvailableCopy(boolean available) {
		return (root, query, cb) -> {
			Subquery<Long> subquery = query.subquery(Long.class);
			Root<BookCopy> copy = subquery.from(BookCopy.class);
			subquery.select(copy.get("id"))
					.where(cb.equal(copy.get("book"), root),
							cb.equal(copy.get("status"), CopyStatus.AVAILABLE));

			return available ? cb.exists(subquery) : cb.not(cb.exists(subquery));
		};
	}

	private static boolean hasText(String value) {
		return value != null && !value.isBlank();
	}

	private static String escapeLike(String value) {
		return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
	}
}
