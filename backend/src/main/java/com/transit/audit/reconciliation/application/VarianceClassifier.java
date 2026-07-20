package com.transit.audit.reconciliation.application;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.transit.audit.reconciliation.domain.model.ReconciliationStatus;

public final class VarianceClassifier {

	private VarianceClassifier() {
	}

	public static BigDecimal variance(BigDecimal actualClosingBalance, BigDecimal expectedClosingBalance) {
		BigDecimal actual = scale(actualClosingBalance);
		BigDecimal expected = scale(expectedClosingBalance);
		return actual.subtract(expected);
	}

	public static ReconciliationStatus classify(BigDecimal variance, BigDecimal smallVarianceThreshold) {
		BigDecimal scaledVariance = scale(variance);
		if (scaledVariance.compareTo(BigDecimal.ZERO) == 0) {
			return ReconciliationStatus.MATCHED;
		}
		BigDecimal threshold = scale(smallVarianceThreshold);
		if (scaledVariance.abs().compareTo(threshold) <= 0) {
			return ReconciliationStatus.SMALL_VARIANCE;
		}
		return ReconciliationStatus.LARGE_VARIANCE;
	}

	private static BigDecimal scale(BigDecimal value) {
		if (value == null) {
			throw new IllegalArgumentException("amount must not be null");
		}
		return value.setScale(2, RoundingMode.HALF_UP);
	}
}
