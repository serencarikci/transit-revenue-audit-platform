package com.transit.audit.common.web;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.transit.audit.common.exception.BusinessException;

class IfMatchSupportTest {

	@Test
	void allowsMissingHeader() {
		assertThatCode(() -> IfMatchSupport.requireMatchesBodyVersion(null, 3L)).doesNotThrowAnyException();
		assertThatCode(() -> IfMatchSupport.requireMatchesBodyVersion("  ", 3L)).doesNotThrowAnyException();
	}

	@Test
	void acceptsMatchingQuotedVersion() {
		assertThatCode(() -> IfMatchSupport.requireMatchesBodyVersion("\"3\"", 3L)).doesNotThrowAnyException();
	}

	@Test
	void rejectsMismatch() {
		assertThatThrownBy(() -> IfMatchSupport.requireMatchesBodyVersion("2", 3L))
				.isInstanceOf(BusinessException.class).hasMessageContaining("If-Match");
	}
}
