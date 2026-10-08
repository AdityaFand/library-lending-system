package com.library.controller;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.library.dto.PageResponse;
import com.library.dto.loan.LoanResponse;
import com.library.dto.me.FineSummaryResponse;
import com.library.dto.me.NotificationResponse;
import com.library.dto.reservation.ReservationResponse;
import com.library.enums.LoanStatus;
import com.library.enums.ReservationStatus;
import com.library.service.MeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MEMBER')")
@Tag(name = "My Account", description = "The logged in member's own loans, fines, reservations and notifications")
public class MeController {

	private final MeService meService;

	@GetMapping("/loans")
	@Operation(summary = "My loans with due dates and fines, optionally filtered by status")
	public PageResponse<LoanResponse> myLoans(
			@RequestParam(required = false) LoanStatus status,
			@ParameterObject @PageableDefault(size = 10, sort = "issueDate", direction = Sort.Direction.DESC) Pageable pageable) {
		return meService.myLoans(status, pageable);
	}

	@GetMapping("/fines")
	@Operation(summary = "My total fines")
	public FineSummaryResponse myFines() {
		return meService.myFines();
	}

	@GetMapping("/reservations")
	@Operation(summary = "My reservations with queue position, optionally filtered by status")
	public PageResponse<ReservationResponse> myReservations(
			@RequestParam(required = false) ReservationStatus status,
			@ParameterObject @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
		return meService.myReservations(status, pageable);
	}

	@GetMapping("/notifications")
	@Operation(summary = "My notifications, newest first")
	public PageResponse<NotificationResponse> myNotifications(
			@RequestParam(defaultValue = "false") boolean unreadOnly,
			@ParameterObject @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
		return meService.myNotifications(unreadOnly, pageable);
	}

	@PostMapping("/notifications/{id}/read")
	@Operation(summary = "Mark one of my notifications as read")
	public NotificationResponse markRead(@PathVariable Long id) {
		return meService.markNotificationRead(id);
	}
}
