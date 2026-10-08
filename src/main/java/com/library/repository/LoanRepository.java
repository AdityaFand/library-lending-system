package com.library.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.library.entity.Loan;
import com.library.enums.LoanStatus;

import jakarta.persistence.LockModeType;

public interface LoanRepository extends JpaRepository<Loan, Long> {

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select l from Loan l where l.id = :id")
	Optional<Loan> findByIdForUpdate(@Param("id") Long id);

	boolean existsByCopyBookId(Long bookId);

	long countByMemberIdAndStatusIn(Long memberId, Collection<LoanStatus> statuses);

	boolean existsByMemberIdAndReturnDateIsNullAndDueDateBefore(Long memberId, LocalDate date);

	boolean existsByMemberIdAndCopyBookIdAndReturnDateIsNull(Long memberId, Long bookId);

	@Query("""
			select l.member.id as memberId,
			       count(l) as openLoans,
			       sum(case when l.dueDate < :today then 1 else 0 end) as overdueLoans
			from Loan l
			where l.member.id in :memberIds and l.returnDate is null
			group by l.member.id
			""")
	List<MemberLoanCountView> countOpenLoansByMemberIds(@Param("memberIds") Collection<Long> memberIds,
			@Param("today") LocalDate today);

	@EntityGraph(attributePaths = { "copy", "copy.book", "member" })
	Optional<Loan> findWithDetailsById(Long id);

	@Query("""
			select l.id from Loan l
			where l.status = com.library.enums.LoanStatus.ACTIVE and l.dueDate < :today
			order by l.id
			""")
	List<Long> findOverdueCandidateIds(@Param("today") LocalDate today);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("""
			update Loan l set l.status = com.library.enums.LoanStatus.OVERDUE
			where l.id = :id and l.status = com.library.enums.LoanStatus.ACTIVE
			""")
	int markOverdueIfActive(@Param("id") Long id);

	@EntityGraph(attributePaths = { "copy", "copy.book", "member" })
	List<Loan> findByIdIn(Collection<Long> ids);

	@Override
	@EntityGraph(attributePaths = { "copy", "copy.book", "member" })
	Page<Loan> findAll(Pageable pageable);

	@EntityGraph(attributePaths = { "copy", "copy.book", "member" })
	Page<Loan> findByStatus(LoanStatus status, Pageable pageable);

	@EntityGraph(attributePaths = { "copy", "copy.book", "member" })
	Page<Loan> findByMemberId(Long memberId, Pageable pageable);

	@EntityGraph(attributePaths = { "copy", "copy.book", "member" })
	Page<Loan> findByMemberIdAndStatus(Long memberId, LoanStatus status, Pageable pageable);

	@Query("select coalesce(sum(l.fine), 0) from Loan l where l.member.id = :memberId")
	BigDecimal sumFinesByMemberId(@Param("memberId") Long memberId);

	long countByMemberIdAndFineGreaterThan(Long memberId, BigDecimal amount);
}
