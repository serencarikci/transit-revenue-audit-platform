package com.transit.audit.common.logging;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SensitiveDataMaskerTest {

	@Test
	void masksPasswordAssignments() {
		assertThat(SensitiveDataMasker.mask("login password=ChangeMe123! ok"))
				.contains("password=***")
				.doesNotContain("ChangeMe123!");
	}

	@Test
	void masksBearerTokens() {
		assertThat(SensitiveDataMasker.mask("Authorization Bearer eyJhbGciOiJIUzI1NiJ9.abc.def"))
				.contains("Bearer ***")
				.doesNotContain("eyJhbGciOiJIUzI1NiJ9");
	}

	@Test
	void masksRawCardAliasAssignments() {
		assertThat(SensitiveDataMasker.mask("cardAlias=SlF412349012"))
				.contains("cardAlias=****")
				.doesNotContain("SlF412349012");
	}

	@Test
	void leavesAlreadyMaskedCardAliasAlone() {
		assertThat(SensitiveDataMasker.mask("cardAlias=SlF4****9012")).contains("SlF4****9012");
	}
}
