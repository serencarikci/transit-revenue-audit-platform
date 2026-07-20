package com.transit.audit.common.concurrency;

import com.transit.audit.common.exception.BusinessException;

public final class OptimisticLockSupport {

	private OptimisticLockSupport() {
	}

	public static void requireVersion(Long actualVersion, Long expectedVersion, String entityLabel) {
		if (expectedVersion == null || !expectedVersion.equals(actualVersion)) {
			throw new BusinessException("Conflict",
					entityLabel + " was changed by another user. Please reload and try again.", 409);
		}
	}
}
