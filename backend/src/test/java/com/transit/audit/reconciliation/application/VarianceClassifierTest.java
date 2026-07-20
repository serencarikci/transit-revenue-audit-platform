package com.transit.audit.reconciliation.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.transit.audit.reconciliation.domain.model.ReconciliationStatus;

class VarianceClassifierTest {

	private static final BigDecimal THRESHOLD = new BigDecimal("1.00");

	@Test
	void zeroVariance_isMatched() {
		BigDecimal variance = VarianceClassifier.variance(new BigDecimal("130.00"), new BigDecimal("130.00"));
		assertThat(variance).isEqualByComparingTo("0.00");
		assertThat(VarianceClassifier.classify(variance, THRESHOLD)).isEqualTo(ReconciliationStatus.MATCHED);
	}

	@Test
	void positiveSmallVariance_isSmallVariance() {
		BigDecimal expected = ExpectedClosingBalanceCalculator.calculate(new BigDecimal("100.00"),
				new BigDecimal("50.00"), new BigDecimal("20.00"));
		BigDecimal variance = VarianceClassifier.variance(new BigDecimal("130.50"), expected);
		assertThat(variance).isEqualByComparingTo("0.50");
		assertThat(VarianceClassifier.classify(variance, THRESHOLD)).isEqualTo(ReconciliationStatus.SMALL_VARIANCE);
	}

	@Test
	void negativeSmallVariance_isSmallVariance() {
		BigDecimal expected = ExpectedClosingBalanceCalculator.calculate(new BigDecimal("100.00"),
				new BigDecimal("50.00"), new BigDecimal("20.00"));
		BigDecimal variance = VarianceClassifier.variance(new BigDecimal("129.25"), expected);
		assertThat(variance).isEqualByComparingTo("-0.75");
		assertThat(VarianceClassifier.classify(variance, THRESHOLD)).isEqualTo(ReconciliationStatus.SMALL_VARIANCE);
	}

	@Test
	void positiveLargeVariance_isLargeVariance() {
		BigDecimal expected = ExpectedClosingBalanceCalculator.calculate(new BigDecimal("100.00"),
				new BigDecimal("50.00"), new BigDecimal("20.00"));
		BigDecimal variance = VarianceClassifier.variance(new BigDecimal("140.00"), expected);
		assertThat(variance).isEqualByComparingTo("10.00");
		assertThat(VarianceClassifier.classify(variance, THRESHOLD)).isEqualTo(ReconciliationStatus.LARGE_VARIANCE);
	}

	@Test
	void negativeLargeVariance_isLargeVariance() {
		BigDecimal expected = ExpectedClosingBalanceCalculator.calculate(new BigDecimal("100.00"),
				new BigDecimal("50.00"), new BigDecimal("20.00"));
		BigDecimal variance = VarianceClassifier.variance(new BigDecimal("120.00"), expected);
		assertThat(variance).isEqualByComparingTo("-10.00");
		assertThat(VarianceClassifier.classify(variance, THRESHOLD)).isEqualTo(ReconciliationStatus.LARGE_VARIANCE);
	}

	@Test
	void thresholdBoundary_isStillSmallVariance() {
		assertThat(VarianceClassifier.classify(new BigDecimal("1.00"), THRESHOLD))
				.isEqualTo(ReconciliationStatus.SMALL_VARIANCE);
		assertThat(VarianceClassifier.classify(new BigDecimal("-1.00"), THRESHOLD))
				.isEqualTo(ReconciliationStatus.SMALL_VARIANCE);
	}
}
