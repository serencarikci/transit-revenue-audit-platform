package com.transit.audit.common.upload;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Set;

import org.springframework.web.multipart.MultipartFile;

import com.transit.audit.common.exception.BusinessException;

public final class CsvUploadValidator {

	public static final long MAX_BYTES = 20L * 1024 * 1024;
	private static final int SNIFF_BYTES = 8192;
	private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("text/csv", "application/csv", "text/plain",
			"application/vnd.ms-excel", "application/octet-stream");

	private CsvUploadValidator() {
	}

	public static void validate(MultipartFile file, String expectedHeaderPrefix) {
		if (file == null || file.isEmpty()) {
			throw new BusinessException("Bad Request", "CSV file is required", 400);
		}
		if (file.getSize() > MAX_BYTES) {
			throw new BusinessException("Bad Request", "CSV file is too large (max 20MB)", 400);
		}

		String name = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase(Locale.ROOT);
		if (!name.endsWith(".csv")) {
			throw new BusinessException("Bad Request", "File name must end with .csv", 400);
		}

		String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase(Locale.ROOT).trim();
		if (!contentType.isEmpty()) {
			String baseType = contentType.split(";", 2)[0].trim();
			if (!ALLOWED_CONTENT_TYPES.contains(baseType) && !baseType.contains("csv")) {
				throw new BusinessException("Bad Request", "Unsupported Content-Type for CSV: " + baseType, 400);
			}
		}

		byte[] head = sniff(file);
		if (containsNullByte(head)) {
			throw new BusinessException("Bad Request", "File is binary, not CSV text", 400);
		}
		String sample = new String(head, StandardCharsets.UTF_8);
		if (sample.isBlank()) {
			throw new BusinessException("Bad Request", "CSV file is empty", 400);
		}
		String firstLine = sample.lines().findFirst().orElse("").replace("\uFEFF", "").trim();
		if (expectedHeaderPrefix != null && !firstLine.toLowerCase(Locale.ROOT)
				.startsWith(expectedHeaderPrefix.toLowerCase(Locale.ROOT))) {
			throw new BusinessException("Bad Request", "Wrong CSV header. It must start with: " + expectedHeaderPrefix,
					400);
		}
	}

	private static byte[] sniff(MultipartFile file) {
		try (InputStream in = file.getInputStream()) {
			return in.readNBytes(SNIFF_BYTES);
		} catch (IOException ex) {
			throw new BusinessException("Bad Request", "Cannot read uploaded file: " + ex.getMessage(), 400);
		}
	}

	private static boolean containsNullByte(byte[] bytes) {
		for (byte b : bytes) {
			if (b == 0) {
				return true;
			}
		}
		return false;
	}
}
