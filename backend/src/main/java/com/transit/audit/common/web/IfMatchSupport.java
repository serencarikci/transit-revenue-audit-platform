package com.transit.audit.common.web;

import com.transit.audit.common.exception.BusinessException;

public final class IfMatchSupport {

	private IfMatchSupport() {
	}

	public static void requireMatchesBodyVersion(String ifMatch, Long bodyVersion) {
		if (ifMatch == null || ifMatch.isBlank()) {
			return;
		}
		Long headerVersion = parse(ifMatch);
		if (!headerVersion.equals(bodyVersion)) {
			throw new BusinessException("Conflict", "If-Match version does not match body version", 409);
		}
	}

	public static Long parse(String ifMatch) {
		String cleaned = ifMatch.replace("\"", "").trim();
		try {
			return Long.valueOf(cleaned);
		} catch (NumberFormatException ex) {
			throw new BusinessException("Bad Request", "If-Match must be a number", 400);
		}
	}
}
