package com.transit.audit.common.util;

public final class CardAliasMasker {

	private CardAliasMasker() {
	}

	public static String mask(String cardAlias) {
		if (cardAlias == null || cardAlias.isBlank()) {
			return "****";
		}
		String value = cardAlias.trim();
		if (value.length() <= 8) {
			return "*".repeat(value.length());
		}
		return value.substring(0, 4) + "****" + value.substring(value.length() - 4);
	}
}
