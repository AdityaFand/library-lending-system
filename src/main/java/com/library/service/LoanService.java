package com.library.service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.library.dto.PageResponse;
import com.library.dto.loan.IssueLoanRequest;
import com.library.dto.loan.LoanResponse;
import com.library.entity.BookCopy;
import com.library.entity.Loan;
import com.library.entity.User;
import com.library.enums.CopyStatus;
import com.library.enums.LoanStatus;
import com.library.enums.Role;
import com.library.exception.BusinessException;
import com.library.exception.ResourceNotFoundException;
import com.library.repository.BookCopyRepository;
import com.library.repository.LoanRepository;
import com.library.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LoanService {

	static final int LOAN_PERIOD_DAYS = 14;
	static final int MAX_ACTIVE_LOANS = 5;
	static final List<LoanStatus> OPEN_STATUSES = List.of(LoanStatus.ACTIVE, LoanStatus.OVERDUE);

	private final LoanRepository loanRepository;
	private final BookCopyRepository copyRepository;
	private final UserRepository userRepository;
	private final Clock clock;

	@Transactional
	public LoanResponse issue(IssueLoanRequest request) {
		User member = userRepository.findByIdForUpdate(request.memberId())
				.orElseThrow(() -> new ResourceNotFoundException("Member not found with id " + request.memberId()));
		if (member.getRole() != Role.MEMBER) {
			throw new BusinessException("Books can only be issued to members");
		}

		BookCopy copy = copyRepository.findByIdForUpdate(request.copyId())
				.orElseThrow(() -> new ResourceNotFoundException("Copy not found with id " + request.copyId()));

		LocalDate today = LocalDate.now(clock);

		if (loanRepository.existsByMemberIdAndReturnDateIsNullAndDueDateBefore(member.getId(), today)) {
			throw new BusinessException("Member has an overdue book that is not yet returned");
		}

		if (loanRepository.countByMemberIdAndStatusIn(member.getId(), OPEN_STATUSES) >= MAX_ACTIVE_LOANS) {
			throw new BusinessException("Member already has " + MAX_ACTIVE_LOANS + " active loans");
		}

		if (copy.getStatus() != CopyStatus.AVAILABLE) {
			throw new BusinessException("Copy " + copy.getId() + " is not available, current status is " + copy.getStatus());
		}

		copy.setStatus(CopyStatus.ISSUED);

		Loan loan = new Loan();
		loan.setCopy(copy);
		loan.setMember(member);
		loan.setIssueDate(today);
		loan.setDueDate(today.plusDays(LOAN_PERIOD_DAYS));
		loan.setStatus(LoanStatus.ACTIVE);

		return LoanResponse.from(loanRepository.save(loan));
	}

	@Transactional(readOnly = true)
	public LoanResponse getById(Long id) {
		return loanRepository.findWithDetailsById(id)
				.map(LoanResponse::from)
				.orElseThrow(() -> new ResourceNotFoundException("Loan not found with id " + id));
	}

	@Transactional(readOnly = true)
	public PageResponse<LoanResponse> list(LoanStatus status, Pageable pageable) {
		Page<Loan> loans = status == null
				? loanRepository.findAll(pageable)
				: loanRepository.findByStatus(status, pageable);
		return PageResponse.from(loans.map(LoanResponse::from));
	}
}
