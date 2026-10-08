package com.library.controller;

import java.time.LocalDate;
import java.util.List;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.library.dto.PageResponse;
import com.library.dto.report.OverdueLoanResponse;
import com.library.dto.report.TopBookResponse;
import com.library.service.ReportService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@PreAuthorize("hasRole('LIBRARIAN')")
@Tag(name = "Reports", description = "Librarian reports")
public class ReportController {

	private final ReportService reportService;

	@GetMapping("/top-borrowed")
	@Operation(summary = "Top 5 most borrowed books with loans issued between two dates (inclusive)")
	public List<TopBookResponse> topBorrowed(
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
		return reportService.topBorrowedBooks(from, to);
	}

	@GetMapping("/overdue-loans")
	@Operation(summary = "Unreturned loans past their due date with member name, book title, due date and days overdue")
	public PageResponse<OverdueLoanResponse> overdueLoans(
			@ParameterObject @PageableDefault(size = 10, sort = "dueDate", direction = Sort.Direction.ASC) Pageable pageable) {
		return reportService.overdueLoans(pageable);
	}
}
