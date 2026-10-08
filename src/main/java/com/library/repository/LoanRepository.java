package com.library.repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.library.entity.Loan;
import com.library.enums.LoanStatus;

public interface LoanRepository extends JpaRepository<Loan, Long> {

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

	@Override
	@EntityGraph(attributePaths = { "copy", "copy.book", "member" })
	Page<Loan> findAll(Pageable pageable);

	@EntityGraph(attributePaths = { "copy", "copy.book", "member" })
	Page<Loan> findByStatus(LoanStatus status, Pageable pageable);
}
