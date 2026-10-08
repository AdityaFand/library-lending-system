package com.library.dto.report;

import java.math.BigDecimal;
import java.time.LocalDate;

public record OverdueLoanResponse(
		Long loanId,
		Long memberId,
		String memberName,
		String memberEmail,
		Long bookId,
		String bookTitle,
		Long copyId,
		LocalDate issueDate,
		LocalDate dueDate,
		long daysOverdue,
		BigDecimal fineIfReturnedToday) {
}
