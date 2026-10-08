package com.library.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.library.dto.PageResponse;
import com.library.dto.reservation.ReservationResponse;
import com.library.entity.Book;
import com.library.entity.BookCopy;
import com.library.entity.Reservation;
import com.library.entity.User;
import com.library.enums.CopyStatus;
import com.library.enums.ReservationStatus;
import com.library.enums.Role;
import com.library.exception.BusinessException;
import com.library.exception.ResourceNotFoundException;
import com.library.repository.BookCopyRepository;
import com.library.repository.BookRepository;
import com.library.repository.LoanRepository;
import com.library.repository.ReservationRepository;
import com.library.repository.UserRepository;
import com.library.security.CurrentUserProvider;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReservationService {

	static final List<ReservationStatus> ACTIVE_STATUSES = List.of(ReservationStatus.WAITING, ReservationStatus.READY);

	private final ReservationRepository reservationRepository;
	private final BookRepository bookRepository;
	private final BookCopyRepository copyRepository;
	private final LoanRepository loanRepository;
	private final UserRepository userRepository;
	private final NotificationService notificationService;
	private final CurrentUserProvider currentUserProvider;
	private final Clock clock;

	@Transactional
	public ReservationResponse reserve(Long bookId) {
		Long memberId = currentUserProvider.getCurrentUser().getId();
		User member = userRepository.findByIdForUpdate(memberId)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		Book book = bookRepository.findByIdForUpdate(bookId)
				.orElseThrow(() -> new ResourceNotFoundException("Book not found with id " + bookId));

		if (copyRepository.existsByBookIdAndStatus(bookId, CopyStatus.AVAILABLE)) {
			throw new BusinessException("A copy of this book is available and can be borrowed directly");
		}
		if (reservationRepository.existsByBookIdAndMemberIdAndStatusIn(bookId, memberId, ACTIVE_STATUSES)) {
			throw new BusinessException("You already have an active reservation for this book");
		}
		if (loanRepository.existsByMemberIdAndCopyBookIdAndReturnDateIsNull(memberId, bookId)) {
			throw new BusinessException("You already have this book on loan");
		}

		Reservation reservation = new Reservation();
		reservation.setBook(book);
		reservation.setMember(member);
		reservation.setStatus(ReservationStatus.WAITING);
		reservation.setCreatedAt(LocalDateTime.now(clock));
		reservationRepository.save(reservation);

		long position = reservationRepository.countAhead(bookId, reservation.getCreatedAt(), reservation.getId()) + 1;
		return ReservationResponse.from(reservation, position);
	}

	@Transactional
	public ReservationResponse cancel(Long id) {
		User currentUser = currentUserProvider.getCurrentUser();

		Optional<Long> heldCopyId = reservationRepository.findHeldCopyId(id);
		BookCopy heldCopy = heldCopyId.flatMap(copyRepository::findByIdForUpdate).orElse(null);

		Reservation reservation = reservationRepository.findByIdForUpdate(id)
				.orElseThrow(() -> new ResourceNotFoundException("Reservation not found with id " + id));

		if (currentUser.getRole() == Role.MEMBER && !reservation.getMember().getId().equals(currentUser.getId())) {
			throw new ResourceNotFoundException("Reservation not found with id " + id);
		}
		if (!ACTIVE_STATUSES.contains(reservation.getStatus())) {
			throw new BusinessException("Only waiting or ready reservations can be cancelled");
		}

		Long currentHeldCopyId = reservation.getHeldCopy() == null ? null : reservation.getHeldCopy().getId();
		if (!Objects.equals(currentHeldCopyId, heldCopy == null ? null : heldCopy.getId())) {
			throw new BusinessException("Reservation was updated by another request, please retry");
		}

		boolean wasReady = reservation.getStatus() == ReservationStatus.READY;
		reservation.setStatus(ReservationStatus.CANCELLED);

		if (wasReady) {
			assignCopyToNextInLine(heldCopy);
		}

		return ReservationResponse.from(reservation);
	}

	@Transactional(readOnly = true)
	public PageResponse<ReservationResponse> search(ReservationStatus status, Long bookId, Pageable pageable) {
		return PageResponse.from(reservationRepository.search(status, bookId, pageable).map(ReservationResponse::from));
	}

	@Transactional
	public void assignCopyToNextInLine(BookCopy copy) {
		Book book = bookRepository.findByIdForUpdate(copy.getBook().getId())
				.orElseThrow(() -> new ResourceNotFoundException("Book not found"));

		Optional<Reservation> next = reservationRepository
				.findFirstByBookIdAndStatusOrderByCreatedAtAscIdAsc(book.getId(), ReservationStatus.WAITING);

		if (next.isEmpty()) {
			copy.setStatus(CopyStatus.AVAILABLE);
			return;
		}

		Reservation reservation = next.get();
		reservation.setStatus(ReservationStatus.READY);
		reservation.setHeldCopy(copy);
		copy.setStatus(CopyStatus.RESERVED);

		notificationService.notify(reservation.getMember(),
				"Your reserved book '" + book.getTitle() + "' is ready for pickup. Copy #" + copy.getId()
						+ " is held for you.");
	}
}
