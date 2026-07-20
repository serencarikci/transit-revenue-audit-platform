package com.transit.audit.transaction.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import com.transit.audit.terminal.domain.model.Terminal;
import com.transit.audit.terminal.infrastructure.persistence.TerminalRepository;
import com.transit.audit.transaction.domain.model.CardTransaction;
import com.transit.audit.transaction.infrastructure.persistence.TransactionRepository;
import com.transit.audit.transaction.web.response.TransactionImportResult;

@ExtendWith(MockitoExtension.class)
class TransactionImportServiceTest {

	@Mock
	private TransactionRepository transactionRepository;

	@Mock
	private TerminalRepository terminalRepository;

	private TransactionImportService importService;

	@BeforeEach
	void setUp() {
		importService = new TransactionImportService(transactionRepository, terminalRepository);
	}

	@Test
	void importCsv_importsValidRow() {
		Terminal terminal = terminal("3122");
		when(terminalRepository.findByTerminalNumber("3122")).thenReturn(Optional.of(terminal));
		when(transactionRepository.existsByTransactionReference(any())).thenReturn(false);
		when(transactionRepository.existsByApprovalNumberAndCardAliasAndTerminalIdAndAmount(any(), any(), any(),
				any())).thenReturn(false);
		when(transactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

		String csv = """
				transactionReference,approvalNumber,cardAlias,terminalNumber,transactionType,productType,amount,transactionTime,sourceSystem
				REF-1,APR-1,SlF412349012,3122,SALE,TICKET,10.50,2026-05-01T10:00:00Z,CSV_IMPORT
				""";
		TransactionImportResult result = importService.importCsv(csvFile(csv));

		assertThat(result.importedCount()).isEqualTo(1);
		assertThat(result.skippedDuplicateCount()).isZero();
		assertThat(result.epochDatedCount()).isZero();
		assertThat(result.errors()).isEmpty();
	}

	@Test
	void importCsv_detectsRule3EpochDatedTransaction() {
		Terminal terminal = terminal("3122");
		when(terminalRepository.findByTerminalNumber("3122")).thenReturn(Optional.of(terminal));
		when(transactionRepository.existsByTransactionReference(any())).thenReturn(false);
		when(transactionRepository.existsByApprovalNumberAndCardAliasAndTerminalIdAndAmount(any(), any(), any(),
				any())).thenReturn(false);
		when(transactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

		String csv = """
				transactionReference,approvalNumber,cardAlias,terminalNumber,transactionType,productType,amount,transactionTime,sourceSystem
				REF-E,APR-E,SlF412349012,3122,SALE,TICKET,5.00,1970-01-01T12:00:00Z,CSV_IMPORT
				""";
		TransactionImportResult result = importService.importCsv(csvFile(csv));

		assertThat(result.importedCount()).isEqualTo(1);
		assertThat(result.epochDatedCount()).isEqualTo(1);
	}

	@Test
	void importCsv_skipsRule4DuplicateFingerprint() {
		Terminal terminal = terminal("3122");
		when(terminalRepository.findByTerminalNumber("3122")).thenReturn(Optional.of(terminal));
		when(transactionRepository.existsByTransactionReference("REF-2")).thenReturn(false);
		when(transactionRepository.existsByApprovalNumberAndCardAliasAndTerminalIdAndAmount(eq("APR-1"),
				eq("SlF412349012"), eq(1L), eq(new BigDecimal("10.50")))).thenReturn(true);

		String csv = """
				transactionReference,approvalNumber,cardAlias,terminalNumber,transactionType,productType,amount,transactionTime,sourceSystem
				REF-2,APR-1,SlF412349012,3122,SALE,TICKET,10.50,2026-05-01T10:00:00Z,CSV_IMPORT
				""";
		TransactionImportResult result = importService.importCsv(csvFile(csv));

		assertThat(result.skippedDuplicateCount()).isEqualTo(1);
		assertThat(result.importedCount()).isZero();
		verify(transactionRepository, never()).save(any(CardTransaction.class));
	}

	@Test
	void isEpochDated_trueOnlyFor1970_01_01() {
		assertThat(TransactionService.isEpochDated(Instant.parse("1970-01-01T23:59:59Z"))).isTrue();
		assertThat(TransactionService.isEpochDated(Instant.parse("1970-01-02T00:00:00Z"))).isFalse();
	}

	private static Terminal terminal(String number) {
		Terminal terminal = new Terminal(number, "SN-" + number);
		ReflectionTestUtils.setField(terminal, "id", 1L);
		return terminal;
	}

	private static MockMultipartFile csvFile(String content) {
		return new MockMultipartFile("file", "tx.csv", "text/csv", content.getBytes(StandardCharsets.UTF_8));
	}
}
