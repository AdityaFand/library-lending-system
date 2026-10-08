package com.library.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.library.service.OverdueLoanService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OverdueLoanJob {

	private final OverdueLoanService overdueLoanService;

	@Scheduled(cron = "${app.overdue-job.cron}")
	public void run() {
		overdueLoanService.markOverdueLoans();
	}
}
