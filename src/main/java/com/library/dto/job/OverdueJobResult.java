package com.library.dto.job;

public record OverdueJobResult(
		int loansMarkedOverdue,
		int membersNotified) {
}
