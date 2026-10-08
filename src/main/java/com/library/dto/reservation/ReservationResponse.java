package com.library.dto.reservation;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.library.entity.Reservation;
import com.library.enums.ReservationStatus;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ReservationResponse(
		Long id,
		Long bookId,
		String bookTitle,
		Long memberId,
		String memberName,
		ReservationStatus status,
		LocalDateTime createdAt,
		Long heldCopyId,
		Long queuePosition) {

	public static ReservationResponse from(Reservation reservation) {
		return from(reservation, null);
	}

	public static ReservationResponse from(Reservation reservation, Long queuePosition) {
		return new ReservationResponse(
				reservation.getId(),
				reservation.getBook().getId(),
				reservation.getBook().getTitle(),
				reservation.getMember().getId(),
				reservation.getMember().getName(),
				reservation.getStatus(),
				reservation.getCreatedAt(),
				reservation.getHeldCopy() == null ? null : reservation.getHeldCopy().getId(),
				queuePosition);
	}
}
