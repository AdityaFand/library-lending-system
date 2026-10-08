package com.library.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.springframework.stereotype.Component;

@Component
public class FineCalculator {

	static final BigDecimal FINE_PER_DAY = BigDecimal.valueOf(5);

	public long daysLate(LocalDate dueDate, LocalDate returnDate) {
		return Math.max(0, ChronoUnit.DAYS.between(dueDate, returnDate));
	}

	public BigDecimal calculate(LocalDate dueDate, LocalDate returnDate, BigDecimal bookPrice) {
		BigDecimal fine = FINE_PER_DAY.multiply(BigDecimal.valueOf(daysLate(dueDate, returnDate)));
		return fine.min(bookPrice).setScale(2);
	}
}
