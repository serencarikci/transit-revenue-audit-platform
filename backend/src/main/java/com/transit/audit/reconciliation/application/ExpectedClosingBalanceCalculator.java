package com.transit.audit.reconciliation.application;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class ExpectedClosingBalanceCalculator {

	private ExpectedClosingBalanceCalculator() {
	}

	public static BigDecimal calculate(BigDecimal openingBalance, BigDecimal depositedAmount,
			BigDecimal withdrawalAmount) {
		BigDecimal opening = scale(openingBalance);
		BigDecimal deposited = scale(depositedAmount);
		BigDecimal withdrawal = scale(withdrawalAmount);
		return opening.add(deposited).subtract(withdrawal);
	}

	private static BigDecimal scale(BigDecimal value) {
		if (value == null) {
			throw new IllegalArgumentException("amount must not be null");
		}
		return value.setScale(2, RoundingMode.HALF_UP);
	}
}
