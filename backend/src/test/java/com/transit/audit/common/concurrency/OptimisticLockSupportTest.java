package com.transit.audit.common.concurrency;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.transit.audit.common.exception.BusinessException;

class OptimisticLockSupportTest {

	@Test
	void acceptsMatchingVersion() {
		assertThatCode(() -> OptimisticLockSupport.requireVersion(3L, 3L, "Depot")).doesNotThrowAnyException();
	}

	@Test
	void rejectsMismatch() {
		assertThatThrownBy(() -> OptimisticLockSupport.requireVersion(3L, 2L, "Depot"))
				.isInstanceOf(BusinessException.class)
				.hasMessageContaining("Depot was changed");
	}

	@Test
	void rejectsNullExpected() {
		assertThatThrownBy(() -> OptimisticLockSupport.requireVersion(1L, null, "User"))
				.isInstanceOf(BusinessException.class);
	}
}
