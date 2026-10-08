package com.library.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.library.entity.BookCopy;
import com.library.enums.CopyStatus;

import jakarta.persistence.LockModeType;

public interface BookCopyRepository extends JpaRepository<BookCopy, Long> {

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select c from BookCopy c join fetch c.book where c.id = :id")
	Optional<BookCopy> findByIdForUpdate(@Param("id") Long id);

	Page<BookCopy> findByBookId(Long bookId, Pageable pageable);

	Page<BookCopy> findByBookIdAndStatus(Long bookId, CopyStatus status, Pageable pageable);

	boolean existsByBookIdAndStatus(Long bookId, CopyStatus status);

	void deleteByBookId(Long bookId);

	@Query("""
			select c.book.id as bookId,
			       count(c) as total,
			       sum(case when c.status = com.library.enums.CopyStatus.AVAILABLE then 1 else 0 end) as available
			from BookCopy c
			where c.book.id in :bookIds
			group by c.book.id
			""")
	List<CopyCountView> countCopiesByBookIds(@Param("bookIds") Collection<Long> bookIds);
}
