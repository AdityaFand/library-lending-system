package com.library.dto.loan;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record IssueLoanRequest(
		@NotNull @Positive Long copyId,
		@NotNull @Positive Long memberId) {
}
