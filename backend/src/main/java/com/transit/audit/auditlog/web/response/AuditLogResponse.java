package com.transit.audit.auditlog.web.response;

import java.time.Instant;

public record AuditLogResponse(Long id, String action, String entityType, String entityId, String actorUsername,
		String detailsJson, Instant createdAt) {
}
