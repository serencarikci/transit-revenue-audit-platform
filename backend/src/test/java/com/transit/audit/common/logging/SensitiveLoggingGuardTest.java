package com.transit.audit.common.logging;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

class SensitiveLoggingGuardTest {

	private static final Path MAIN_JAVA = Path.of("src/main/java");

	private static final Pattern LOG_START = Pattern.compile("\\blog\\.(trace|debug|info|warn|error)\\s*\\(");

	@Test
	void productionLogsMustNotLeakSecrets() throws IOException {
		List<String> violations = new ArrayList<>();
		try (Stream<Path> paths = Files.walk(MAIN_JAVA)) {
			paths.filter(p -> p.toString().endsWith(".java")).forEach(path -> {
				try {
					String source = Files.readString(path);
					String[] lines = source.split("\\R");
					for (int i = 0; i < lines.length; i++) {
						Matcher start = LOG_START.matcher(lines[i]);
						if (!start.find()) {
							continue;
						}
						String statement = collectStatement(lines, i);
						String lower = statement.toLowerCase();
						if (lower.matches("(?s).*\\bpassword\\b(?!encoder|hash|_hash).*")
								&& !statement.contains("passwordEncoder")
								&& !statement.contains("PasswordEncoder")) {

							if (statement.matches("(?s).*\\b(password|passwd|pwd)\\s*[=:].*")
									|| statement.matches("(?s).*\\bpassword\\b\\s*,.*")
									|| statement.contains("request.password()")
									|| statement.contains(".getPassword()")
									|| statement.contains("password)")) {
								violations.add(path + ":" + (i + 1) + " password in log: " + summarize(statement));
							}
						}
						if (lower.contains("accesstoken") || lower.contains("refreshtoken")
								|| statement.contains("getTokenValue(")) {
							violations.add(path + ":" + (i + 1) + " token in log: " + summarize(statement));
						}
						if (statement.contains("cardAlias") && !statement.contains("CardAliasMasker")) {
							violations.add(path + ":" + (i + 1) + " unmasked cardAlias in log: " + summarize(statement));
						}
					}
				} catch (IOException ex) {
					throw new RuntimeException(ex);
				}
			});
		}
		assertThat(violations).as("sensitive logging violations").isEmpty();
	}

	private static String collectStatement(String[] lines, int start) {
		StringBuilder sb = new StringBuilder();
		for (int i = start; i < lines.length; i++) {
			sb.append(lines[i]).append('\n');
			if (lines[i].contains(";")) {
				break;
			}
		}
		return sb.toString();
	}

	private static String summarize(String statement) {
		return statement.replaceAll("\\s+", " ").trim();
	}
}
