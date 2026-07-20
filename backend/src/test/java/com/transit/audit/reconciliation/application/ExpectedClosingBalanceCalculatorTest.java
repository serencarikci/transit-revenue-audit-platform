package com.transit.audit.reconciliation.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class ExpectedClosingBalanceCalculatorTest {

	@Test
	void rule1_calculatesOpeningPlusDepositedMinusWithdrawal() {
		BigDecimal expected = ExpectedClosingBalanceCalculator.calculate(new BigDecimal("1000.00"),
				new BigDecimal("250.50"), new BigDecimal("100.25"));
		assertThat(expected).isEqualByComparingTo("1150.25");
	}

	@Test
	void zeroVariance_whenActualEqualsExpected() {
		BigDecimal expected = ExpectedClosingBalanceCalculator.calculate(new BigDecimal("100.00"),
				new BigDecimal("50.00"), new BigDecimal("20.00"));
		BigDecimal variance = VarianceClassifier.variance(new BigDecimal("130.00"), expected);
		assertThat(expected).isEqualByComparingTo("130.00");
		assertThat(variance).isEqualByComparingTo("0.00");
	}

	@Test
	void positiveVariance_whenActualGreaterThanExpected() {
		BigDecimal expected = ExpectedClosingBalanceCalculator.calculate(new BigDecimal("100.00"),
				new BigDecimal("50.00"), new BigDecimal("20.00"));
		BigDecimal variance = VarianceClassifier.variance(new BigDecimal("135.00"), expected);
		assertThat(variance).isEqualByComparingTo("5.00");
	}

	@Test
	void negativeVariance_whenActualLessThanExpected() {
		BigDecimal expected = ExpectedClosingBalanceCalculator.calculate(new BigDecimal("100.00"),
				new BigDecimal("50.00"), new BigDecimal("20.00"));
		BigDecimal variance = VarianceClassifier.variance(new BigDecimal("125.00"), expected);
		assertThat(variance).isEqualByComparingTo("-5.00");
	}
}
