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
import com.library.dto.reservation.ReservationRequest;
import com.library.dto.reservation.ReservationResponse;
import com.library.enums.ReservationStatus;
import com.library.service.ReservationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
@Tag(name = "Reservations", description = "Reserve books that have no free copy")
public class ReservationController {

	private final ReservationService reservationService;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("hasRole('MEMBER')")
	@Operation(summary = "Reserve a book that has no available copy (member)")
	public ReservationResponse reserve(@Valid @RequestBody ReservationRequest request) {
		return reservationService.reserve(request.bookId());
	}

	@PostMapping("/{id}/cancel")
	@PreAuthorize("hasAnyRole('MEMBER', 'LIBRARIAN')")
	@Operation(summary = "Cancel a reservation (own reservation for members, any for librarians)")
	public ReservationResponse cancel(@PathVariable Long id) {
		return reservationService.cancel(id);
	}

	@GetMapping
	@PreAuthorize("hasRole('LIBRARIAN')")
	@Operation(summary = "List reservations, optionally filtered by status and book (librarian)")
	public PageResponse<ReservationResponse> search(
			@RequestParam(required = false) ReservationStatus status,
			@RequestParam(required = false) Long bookId,
			@ParameterObject @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.ASC) Pageable pageable) {
		return reservationService.search(status, bookId, pageable);
	}
}
