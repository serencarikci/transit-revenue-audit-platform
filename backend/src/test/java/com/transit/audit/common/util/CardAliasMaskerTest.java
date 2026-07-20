package com.transit.audit.common.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class CardAliasMaskerTest {

	@Test
	void masksMiddleKeepingFirstAndLastFour() {
		assertThat(CardAliasMasker.mask("SlF412349012")).isEqualTo("SlF4****9012");
	}

	@Test
	void masksShortAliasesFully() {
		assertThat(CardAliasMasker.mask("ABCD")).isEqualTo("****");
	}
}
