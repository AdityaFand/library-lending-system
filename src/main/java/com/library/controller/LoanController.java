package com.library.controller;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.library.dto.PageResponse;
import com.library.dto.loan.IssueLoanRequest;
import com.library.dto.loan.LoanResponse;
import com.library.enums.LoanStatus;
import com.library.service.LoanService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
@PreAuthorize("hasRole('LIBRARIAN')")
@Tag(name = "Loans", description = "Issue books and view loans (librarian)")
public class LoanController {

	private final LoanService loanService;

	@PostMapping("/issue")
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Issue an available copy to a member, due in 14 days")
	public LoanResponse issue(@Valid @RequestBody IssueLoanRequest request) {
		return loanService.issue(request);
	}

	@PostMapping("/{id}/return")
	@Operation(summary = "Return a loan, calculate the fine and pass the copy to the next reservation")
	public LoanResponse returnLoan(@PathVariable Long id) {
		return loanService.returnLoan(id);
	}

	@GetMapping("/{id}")
	@Operation(summary = "View a loan")
	public LoanResponse getById(@PathVariable Long id) {
		return loanService.getById(id);
	}

	@GetMapping
	@Operation(summary = "List loans, optionally filtered by status")
	public PageResponse<LoanResponse> list(
			@RequestParam(required = false) LoanStatus status,
			@ParameterObject @PageableDefault(size = 10, sort = "issueDate", direction = Sort.Direction.DESC) Pageable pageable) {
		return loanService.list(status, pageable);
	}
}
