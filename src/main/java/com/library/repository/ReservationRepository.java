package com.library.repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.library.entity.Reservation;
import com.library.enums.ReservationStatus;

import jakarta.persistence.LockModeType;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

	boolean existsByBookId(Long bookId);

	boolean existsByBookIdAndMemberIdAndStatusIn(Long bookId, Long memberId, Collection<ReservationStatus> statuses);

	Optional<Reservation> findFirstByBookIdAndStatusOrderByCreatedAtAscIdAsc(Long bookId, ReservationStatus status);

	Optional<Reservation> findByHeldCopyIdAndStatus(Long copyId, ReservationStatus status);

	Optional<Reservation> findFirstByBookIdAndMemberIdAndStatus(Long bookId, Long memberId, ReservationStatus status);

	@Query("select hc.id from Reservation r join r.heldCopy hc where r.id = :id")
	Optional<Long> findHeldCopyId(@Param("id") Long id);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select r from Reservation r join fetch r.book join fetch r.member where r.id = :id")
	Optional<Reservation> findByIdForUpdate(@Param("id") Long id);

	@Query("""
			select count(r) from Reservation r
			where r.book.id = :bookId
			  and r.status = com.library.enums.ReservationStatus.WAITING
			  and (r.createdAt < :createdAt or (r.createdAt = :createdAt and r.id < :id))
			""")
	long countAhead(@Param("bookId") Long bookId, @Param("createdAt") LocalDateTime createdAt, @Param("id") Long id);

	@EntityGraph(attributePaths = { "book", "member", "heldCopy" })
	@Query("""
			select r from Reservation r
			where (:status is null or r.status = :status)
			  and (:bookId is null or r.book.id = :bookId)
			""")
	Page<Reservation> search(@Param("status") ReservationStatus status, @Param("bookId") Long bookId,
			Pageable pageable);
}
