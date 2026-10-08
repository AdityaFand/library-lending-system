package com.library.service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.library.dto.job.OverdueJobResult;
import com.library.entity.Loan;
import com.library.entity.User;
import com.library.repository.LoanRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OverdueLoanService {

	private final LoanRepository loanRepository;
	private final NotificationService notificationService;
	private final Clock clock;

	@Transactional
	public OverdueJobResult markOverdueLoans() {
		LocalDate today = LocalDate.now(clock);

		List<Long> markedIds = new ArrayList<>();
		for (Long loanId : loanRepository.findOverdueCandidateIds(today)) {
			if (loanRepository.markOverdueIfActive(loanId) == 1) {
				markedIds.add(loanId);
			}
		}

		if (markedIds.isEmpty()) {
			log.info("Overdue job: no new overdue loans");
			return new OverdueJobResult(0, 0);
		}

		Map<Long, List<Loan>> loansByMember = loanRepository.findByIdIn(markedIds).stream()
				.sorted(Comparator.comparing(Loan::getDueDate))
				.collect(Collectors.groupingBy(loan -> loan.getMember().getId(), LinkedHashMap::new, Collectors.toList()));

		loansByMember.values().forEach(loans -> {
			User member = loans.get(0).getMember();
			notificationService.notify(member, buildMessage(loans));
		});

		log.info("Overdue job: {} loans marked OVERDUE, {} members notified", markedIds.size(), loansByMember.size());
		return new OverdueJobResult(markedIds.size(), loansByMember.size());
	}

	private String buildMessage(List<Loan> loans) {
		String books = loans.stream()
				.map(loan -> "'" + loan.getCopy().getBook().getTitle() + "' (due " + loan.getDueDate() + ")")
				.collect(Collectors.joining(", "));
		if (loans.size() == 1) {
			return "You have 1 overdue book: " + books
					+ ". Please return it as soon as possible, a fine of Rs 5 per day applies.";
		}
		return "You have " + loans.size() + " overdue books: " + books
				+ ". Please return them as soon as possible, a fine of Rs 5 per day applies to each.";
	}
}
