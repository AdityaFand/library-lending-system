package com.library.dto.me;

import java.math.BigDecimal;

public record FineSummaryResponse(
		BigDecimal totalFines,
		long loansWithFine) {
}
