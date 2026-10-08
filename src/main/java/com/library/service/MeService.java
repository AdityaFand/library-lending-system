package com.library.service;

import java.math.BigDecimal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.library.dto.PageResponse;
import com.library.dto.loan.LoanResponse;
import com.library.dto.me.FineSummaryResponse;
import com.library.dto.me.NotificationResponse;
import com.library.dto.reservation.ReservationResponse;
import com.library.entity.Loan;
import com.library.entity.Notification;
import com.library.entity.Reservation;
import com.library.enums.LoanStatus;
import com.library.enums.ReservationStatus;
import com.library.exception.ResourceNotFoundException;
import com.library.repository.LoanRepository;
import com.library.repository.NotificationRepository;
import com.library.repository.ReservationRepository;
import com.library.security.CurrentUserProvider;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MeService {

	private final LoanRepository loanRepository;
	private final ReservationRepository reservationRepository;
	private final NotificationRepository notificationRepository;
	private final CurrentUserProvider currentUserProvider;

	@Transactional(readOnly = true)
	public PageResponse<LoanResponse> myLoans(LoanStatus status, Pageable pageable) {
		Long memberId = currentUserId();
		Page<Loan> loans = status == null
				? loanRepository.findByMemberId(memberId, pageable)
				: loanRepository.findByMemberIdAndStatus(memberId, status, pageable);
		return PageResponse.from(loans.map(LoanResponse::from));
	}

	@Transactional(readOnly = true)
	public FineSummaryResponse myFines() {
		Long memberId = currentUserId();
		return new FineSummaryResponse(
				loanRepository.sumFinesByMemberId(memberId),
				loanRepository.countByMemberIdAndFineGreaterThan(memberId, BigDecimal.ZERO));
	}

	@Transactional(readOnly = true)
	public PageResponse<ReservationResponse> myReservations(ReservationStatus status, Pageable pageable) {
		Long memberId = currentUserId();
		Page<Reservation> reservations = status == null
				? reservationRepository.findByMemberId(memberId, pageable)
				: reservationRepository.findByMemberIdAndStatus(memberId, status, pageable);
		return PageResponse.from(reservations.map(this::toResponseWithPosition));
	}

	@Transactional(readOnly = true)
	public PageResponse<NotificationResponse> myNotifications(boolean unreadOnly, Pageable pageable) {
		Long userId = currentUserId();
		Page<Notification> notifications = unreadOnly
				? notificationRepository.findByUserIdAndReadFalse(userId, pageable)
				: notificationRepository.findByUserId(userId, pageable);
		return PageResponse.from(notifications.map(NotificationResponse::from));
	}

	@Transactional
	public NotificationResponse markNotificationRead(Long id) {
		Notification notification = notificationRepository.findByIdAndUserId(id, currentUserId())
				.orElseThrow(() -> new ResourceNotFoundException("Notification not found with id " + id));
		notification.setRead(true);
		return NotificationResponse.from(notification);
	}

	private ReservationResponse toResponseWithPosition(Reservation reservation) {
		if (reservation.getStatus() != ReservationStatus.WAITING) {
			return ReservationResponse.from(reservation);
		}
		long position = reservationRepository.countAhead(reservation.getBook().getId(),
				reservation.getCreatedAt(), reservation.getId()) + 1;
		return ReservationResponse.from(reservation, position);
	}

	private Long currentUserId() {
		return currentUserProvider.getCurrentUser().getId();
	}
}
