package com.transit.audit.common.upload;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import com.transit.audit.common.exception.BusinessException;

class CsvUploadValidatorTest {

	private static final String HEADER = "transactionReference,approvalNumber,cardAlias,terminalNumber,transactionType,productType,amount,transactionTime";

	@Test
	void acceptsValidCsv() {
		CsvUploadValidator.validate(csv("tx.csv", "text/csv", HEADER + "\nREF,A,CARD,3122,SALE,,1.00,2026-01-01T00:00:00Z"),
				HEADER);
	}

	@Test
	void rejectsNonCsvExtension() {
		assertThatThrownBy(() -> CsvUploadValidator.validate(
				csv("tx.txt", "text/plain", HEADER + "\nx"), HEADER)).isInstanceOf(BusinessException.class)
				.hasMessageContaining(".csv");
	}

	@Test
	void rejectsWrongMimeType() {
		assertThatThrownBy(() -> CsvUploadValidator.validate(
				csv("tx.csv", "application/pdf", HEADER + "\nx"), HEADER)).isInstanceOf(BusinessException.class)
				.hasMessageContaining("Content-Type");
	}

	@Test
	void rejectsBinaryContent() {
		byte[] binary = new byte[] { 0x00, 0x01, 0x02, 'a', 'b' };
		MockMultipartFile file = new MockMultipartFile("file", "tx.csv", "text/csv", binary);
		assertThatThrownBy(() -> CsvUploadValidator.validate(file, HEADER)).isInstanceOf(BusinessException.class)
				.hasMessageContaining("binary");
	}

	@Test
	void rejectsWrongHeader() {
		assertThatThrownBy(() -> CsvUploadValidator.validate(csv("tx.csv", "text/csv", "foo,bar\n1,2"), HEADER))
				.isInstanceOf(BusinessException.class).hasMessageContaining("header");
	}

	private static MockMultipartFile csv(String name, String contentType, String body) {
		return new MockMultipartFile("file", name, contentType, body.getBytes(StandardCharsets.UTF_8));
	}
}
