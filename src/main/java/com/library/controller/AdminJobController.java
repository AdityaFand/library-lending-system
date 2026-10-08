package com.library.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.library.dto.job.OverdueJobResult;
import com.library.service.OverdueLoanService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/jobs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('LIBRARIAN')")
@Tag(name = "Jobs", description = "Run scheduled jobs on demand (librarian)")
public class AdminJobController {

	private final OverdueLoanService overdueLoanService;

	@PostMapping("/overdue")
	@Operation(summary = "Run the daily overdue job now")
	public OverdueJobResult runOverdueJob() {
		return overdueLoanService.markOverdueLoans();
	}
}
