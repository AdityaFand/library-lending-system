package com.library.service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.library.dto.PageResponse;
import com.library.dto.report.OverdueLoanResponse;
import com.library.dto.report.TopBookResponse;
import com.library.entity.Loan;
import com.library.exception.BadRequestException;
import com.library.repository.LoanRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReportService {

	static final int TOP_BOOKS_LIMIT = 5;

	private final LoanRepository loanRepository;
	private final FineCalculator fineCalculator;
	private final Clock clock;

	@Transactional(readOnly = true)
	public List<TopBookResponse> topBorrowedBooks(LocalDate from, LocalDate to) {
		if (from.isAfter(to)) {
			throw new BadRequestException("'from' date must be on or before 'to' date");
		}
		return loanRepository.findTopBorrowedBooks(from, to, PageRequest.of(0, TOP_BOOKS_LIMIT));
	}

	@Transactional(readOnly = true)
	public PageResponse<OverdueLoanResponse> overdueLoans(Pageable pageable) {
		LocalDate today = LocalDate.now(clock);
		return PageResponse.from(loanRepository.findOverdueLoans(today, pageable)
				.map(loan -> toOverdueResponse(loan, today)));
	}

	private OverdueLoanResponse toOverdueResponse(Loan loan, LocalDate today) {
		return new OverdueLoanResponse(
				loan.getId(),
				loan.getMember().getId(),
				loan.getMember().getName(),
				loan.getMember().getEmail(),
				loan.getCopy().getBook().getId(),
				loan.getCopy().getBook().getTitle(),
				loan.getCopy().getId(),
				loan.getIssueDate(),
				loan.getDueDate(),
				fineCalculator.daysLate(loan.getDueDate(), today),
				fineCalculator.calculate(loan.getDueDate(), today, loan.getCopy().getBook().getPrice()));
	}
}
