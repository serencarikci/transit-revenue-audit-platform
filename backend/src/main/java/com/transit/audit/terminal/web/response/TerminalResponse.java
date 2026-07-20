package com.transit.audit.terminal.web.response;

import java.time.Instant;

import com.transit.audit.terminal.domain.model.TerminalStatus;

public record TerminalResponse(Long id, String terminalNumber, String serialNumber, TerminalStatus status,
		Instant lastSyncTime, Instant lastTransactionTime, int pendingTransactionCount, int retryCount, boolean active,
		Instant createdAt, Instant updatedAt, Long version) {
}
