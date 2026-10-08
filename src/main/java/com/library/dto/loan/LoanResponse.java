package com.library.dto.loan;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.library.entity.Loan;
import com.library.enums.LoanStatus;

public record LoanResponse(
		Long id,
		Long copyId,
		Long bookId,
		String bookTitle,
		Long memberId,
		String memberName,
		LocalDate issueDate,
		LocalDate dueDate,
		LocalDate returnDate,
		BigDecimal fine,
		LoanStatus status,
		boolean renewed) {

	public static LoanResponse from(Loan loan) {
		return new LoanResponse(
				loan.getId(),
				loan.getCopy().getId(),
				loan.getCopy().getBook().getId(),
				loan.getCopy().getBook().getTitle(),
				loan.getMember().getId(),
				loan.getMember().getName(),
				loan.getIssueDate(),
				loan.getDueDate(),
				loan.getReturnDate(),
				loan.getFine(),
				loan.getStatus(),
				loan.isRenewed());
	}
}
