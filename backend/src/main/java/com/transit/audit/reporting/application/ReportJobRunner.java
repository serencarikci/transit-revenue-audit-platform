package com.transit.audit.reporting.application;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class ReportJobRunner {

	private static final Logger log = LoggerFactory.getLogger(ReportJobRunner.class);

	private final ReportingService reportingService;

	public ReportJobRunner(ReportingService reportingService) {
		this.reportingService = reportingService;
	}

	@Async
	public void run(Long snapshotId, Instant from, Instant to) {
		try {
			reportingService.generate(snapshotId, from, to);
		} catch (Exception ex) {
			log.error("Report job {} failed: {}", snapshotId, ex.getMessage(), ex);
			reportingService.markFailed(snapshotId, ex.getMessage());
		}
	}
}
