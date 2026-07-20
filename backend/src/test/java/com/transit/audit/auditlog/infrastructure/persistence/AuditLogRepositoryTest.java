package com.transit.audit.auditlog.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

class AuditLogRepositoryTest {

	@Test
	void rule12_repositoryExposesNoDeleteMethods() {
		boolean hasDelete = Arrays.stream(AuditLogRepository.class.getMethods()).map(Method::getName)
				.anyMatch(name -> name.startsWith("delete"));
		assertThat(hasDelete).as("AuditLogRepository must not declare delete* methods").isFalse();
	}
}
