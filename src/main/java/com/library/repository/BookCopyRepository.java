package com.library.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.library.entity.BookCopy;

public interface BookCopyRepository extends JpaRepository<BookCopy, Long> {

	Page<BookCopy> findByBookId(Long bookId, Pageable pageable);

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
