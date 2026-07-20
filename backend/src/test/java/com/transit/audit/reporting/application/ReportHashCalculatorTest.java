package com.transit.audit.reporting.application;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ReportHashCalculatorTest {

	@Test
	void sameInputsProduceSameHash() {
		String h1 = ReportHashCalculator.sha256("{\"a\":1}", "payload");
		String h2 = ReportHashCalculator.sha256("{\"a\":1}", "payload");
		assertThat(h1).isEqualTo(h2);
	}

	@Test
	void differentPayloadProducesDifferentHash_rule11() {
		String h1 = ReportHashCalculator.sha256("{\"a\":1}", "payload-a");
		String h2 = ReportHashCalculator.sha256("{\"a\":1}", "payload-b");
		assertThat(h1).isNotEqualTo(h2);
	}
}
