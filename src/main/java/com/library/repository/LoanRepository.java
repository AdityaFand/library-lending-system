package com.library.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.library.entity.Loan;

public interface LoanRepository extends JpaRepository<Loan, Long> {

	boolean existsByCopyBookId(Long bookId);
}
