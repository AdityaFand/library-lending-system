package com.library.dto.book;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record BookRequest(
		@NotBlank @Size(max = 255) String title,
		@NotBlank @Size(max = 20) String isbn,
		@NotBlank @Size(max = 255) String author,
		@NotBlank @Size(max = 100) String category,
		@NotNull @DecimalMin(value = "0.0", inclusive = false) @Digits(integer = 8, fraction = 2) BigDecimal price) {
}
