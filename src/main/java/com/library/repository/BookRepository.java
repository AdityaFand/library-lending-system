package com.library.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.library.entity.Book;

import jakarta.persistence.LockModeType;

public interface BookRepository extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {

	boolean existsByIsbn(String isbn);

	boolean existsByIsbnAndIdNot(String isbn, Long id);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select b from Book b where b.id = :id")
	Optional<Book> findByIdForUpdate(@Param("id") Long id);
}
