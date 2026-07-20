package com.transit.audit.common.logging;

import java.util.regex.Pattern;

public final class SensitiveDataMasker {

	private static final Pattern PASSWORD = Pattern.compile("(?i)(password|passwd|pwd)\\s*[=:]\\s*[^\\s,;\"']+");
	private static final Pattern TOKEN = Pattern
			.compile("(?i)(access[_-]?token|refresh[_-]?token|id[_-]?token|bearer)\\s*[=:]?\\s*[A-Za-z0-9\\-._~+/]+=*");
	private static final Pattern BEARER = Pattern.compile("(?i)Bearer\\s+[A-Za-z0-9\\-._~+/]+=*");
	private static final Pattern CARD_ALIAS_ASSIGN = Pattern
			.compile("(?i)(cardAlias|card_alias)\\s*[=:]\\s*(?!\\*{2})[A-Za-z0-9]{9,}");

	private SensitiveDataMasker() {
	}

	public static String mask(String message) {
		if (message == null || message.isEmpty()) {
			return message;
		}
		String masked = PASSWORD.matcher(message).replaceAll("$1=***");
		masked = BEARER.matcher(masked).replaceAll("Bearer ***");
		masked = TOKEN.matcher(masked).replaceAll("$1=***");
		masked = CARD_ALIAS_ASSIGN.matcher(masked).replaceAll("$1=****");
		return masked;
	}
}
