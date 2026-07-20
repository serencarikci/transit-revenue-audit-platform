package com.transit.audit.reporting.application;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public final class ReportHashCalculator {

	private ReportHashCalculator() {
	}

	public static String sha256(String parametersJson, String payload) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			digest.update(parametersJson.getBytes(StandardCharsets.UTF_8));
			digest.update((byte) 0);
			digest.update(payload.getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(digest.digest());
		} catch (NoSuchAlgorithmException ex) {
			throw new IllegalStateException("SHA-256 not available", ex);
		}
	}
}
