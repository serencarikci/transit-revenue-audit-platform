package com.transit.audit.reporting.web.response;

import java.time.Instant;

import com.transit.audit.reporting.domain.model.ReportJobStatus;

public record ReportSnapshotResponse(Long id, String reportType, String parametersJson, ReportJobStatus status,
		String resultHash, String outputPath, String errorMessage, String requestedBy, Instant startedAt,
		Instant completedAt, Long version) {
}
