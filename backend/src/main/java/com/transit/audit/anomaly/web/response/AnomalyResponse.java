package com.transit.audit.anomaly.web.response;

import java.time.Instant;

import com.transit.audit.anomaly.domain.model.AnomalySeverity;
import com.transit.audit.anomaly.domain.model.AnomalyStatus;

public record AnomalyResponse(Long id, String ruleCode, AnomalySeverity severity, AnomalyStatus status,
		String entityType, String entityId, String title, String details, Instant detectedAt, String reviewedBy,
		String resolutionNote, Instant resolvedAt, Long version) {
}
