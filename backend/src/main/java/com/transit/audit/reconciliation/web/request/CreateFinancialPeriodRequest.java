package com.transit.audit.reconciliation.web.request;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record CreateFinancialPeriodRequest(@NotNull Long depotId, @NotNull LocalDate periodDate,
		@NotNull @DecimalMin("0.00") BigDecimal openingBalance, @NotNull @DecimalMin("0.00") BigDecimal depositedAmount,
		@NotNull @DecimalMin("0.00") BigDecimal withdrawalAmount, @NotNull BigDecimal actualClosingBalance) {
}
