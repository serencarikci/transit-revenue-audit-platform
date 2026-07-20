package com.transit.audit.terminal.web.response;

import java.time.Instant;
import java.time.LocalDate;

public record AssignmentResponse(Long id, Long terminalId, Long depotId, LocalDate validFrom, LocalDate validTo,
		Instant createdAt) {
}
