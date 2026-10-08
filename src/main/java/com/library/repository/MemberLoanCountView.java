package com.library.repository;

public interface MemberLoanCountView {

	Long getMemberId();

	Long getOpenLoans();

	Long getOverdueLoans();
}
