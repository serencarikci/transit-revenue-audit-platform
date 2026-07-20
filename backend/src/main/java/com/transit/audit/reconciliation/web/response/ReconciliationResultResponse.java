package com.transit.audit.reconciliation.web.response;

import java.math.BigDecimal;
import java.time.Instant;

import com.transit.audit.reconciliation.domain.model.ReconciliationStatus;

public record ReconciliationResultResponse(Long id, Long financialPeriodId, BigDecimal expectedClosingBalance,
		BigDecimal actualClosingBalance, BigDecimal variance, BigDecimal saleAmount, BigDecimal cancellationAmount,
		BigDecimal netAmount, ReconciliationStatus status, String resolutionNote, String resolvedBy, Instant resolvedAt,
		Instant createdAt, Long version) {
}
