package com.transit.audit.reconciliation.web.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import com.transit.audit.reconciliation.domain.model.FinancialPeriodStatus;

public record FinancialPeriodResponse(Long id, Long depotId, LocalDate periodDate, BigDecimal openingBalance,
		BigDecimal depositedAmount, BigDecimal withdrawalAmount, BigDecimal actualClosingBalance,
		FinancialPeriodStatus status, Instant createdAt, Instant updatedAt, Long version) {
}
