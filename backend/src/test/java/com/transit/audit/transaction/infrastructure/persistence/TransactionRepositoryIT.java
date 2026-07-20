package com.transit.audit.transaction.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import com.transit.audit.support.AbstractPostgresIntegrationTest;
import com.transit.audit.terminal.domain.model.Terminal;
import com.transit.audit.terminal.infrastructure.persistence.TerminalRepository;
import com.transit.audit.transaction.application.TransactionService;
import com.transit.audit.transaction.domain.model.CardTransaction;
import com.transit.audit.transaction.domain.model.TransactionType;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
class TransactionRepositoryIT extends AbstractPostgresIntegrationTest {

	@Autowired
	private TransactionRepository transactionRepository;

	@Autowired
	private TerminalRepository terminalRepository;

	@Test
	void rejectsDuplicateFingerprint_rule4() {
		Terminal terminal = terminalRepository.save(new Terminal("T80", "SN-T80"));
		CardTransaction first = new CardTransaction("R1", "AP1", "CARDALIAS001", terminal.getId(), TransactionType.SALE,
				"TICKET", new BigDecimal("12.00"), Instant.parse("2026-05-01T10:00:00Z"), "CSV");
		transactionRepository.saveAndFlush(first);

		CardTransaction dup = new CardTransaction("R2", "AP1", "CARDALIAS001", terminal.getId(), TransactionType.SALE,
				"TICKET", new BigDecimal("12.00"), Instant.parse("2026-05-01T11:00:00Z"), "CSV");

		assertThatThrownBy(() -> transactionRepository.saveAndFlush(dup))
				.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void findsEpochDatedTransactions_rule3() {
		Terminal terminal = terminalRepository.save(new Terminal("T81", "SN-T81"));
		transactionRepository.saveAndFlush(new CardTransaction("RE", "APE", "CARDALIAS002", terminal.getId(),
				TransactionType.SALE, null, new BigDecimal("1.00"), Instant.parse("1970-01-01T08:00:00Z"), "CSV"));
		transactionRepository.saveAndFlush(new CardTransaction("RN", "APN", "CARDALIAS003", terminal.getId(),
				TransactionType.SALE, null, new BigDecimal("1.00"), Instant.parse("2026-05-01T08:00:00Z"), "CSV"));

		assertThat(transactionRepository.findEpochDatedTransactions(TransactionService.EPOCH_DAY_START,
				TransactionService.EPOCH_DAY_END)).hasSize(1);
	}
}
