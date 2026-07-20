package com.transit.audit.anomaly.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.transit.audit.anomaly.domain.model.Anomaly;
import com.transit.audit.anomaly.infrastructure.persistence.AnomalyRepository;
import com.transit.audit.config.AnomalyProperties;
import com.transit.audit.config.PdaProperties;
import com.transit.audit.notification.application.NotificationService;
import com.transit.audit.terminal.domain.model.Terminal;
import com.transit.audit.terminal.infrastructure.persistence.TerminalRepository;
import com.transit.audit.transaction.domain.model.CardTransaction;
import com.transit.audit.transaction.domain.model.TransactionType;
import com.transit.audit.transaction.infrastructure.persistence.TransactionRepository;

@ExtendWith(MockitoExtension.class)
class AnomalyRuleEngineTest {

	@Mock
	private AnomalyRepository anomalyRepository;

	@Mock
	private TerminalRepository terminalRepository;

	@Mock
	private TransactionRepository transactionRepository;

	@Mock
	private NotificationService notificationService;

	private AnomalyRuleEngine engine;

	@BeforeEach
	void setUp() {
		Clock clock = Clock.fixed(Instant.parse("2026-05-02T12:00:00Z"), ZoneOffset.UTC);
		engine = new AnomalyRuleEngine(anomalyRepository, terminalRepository, transactionRepository,
				new PdaProperties(24, 1, "0 5 1 * * *"), new AnomalyProperties(5, 5, 5, 24, 12, "0 10 1 * * *"),
				notificationService, clock);
		when(anomalyRepository.existsByRuleCodeAndEntityTypeAndEntityIdAndStatus(any(), any(), any(), any()))
				.thenReturn(false);
		when(anomalyRepository.save(any(Anomaly.class))).thenAnswer(inv -> inv.getArgument(0));
	}

	@Test
	void rule7_flagsTerminalWithStaleSync() {
		Terminal stale = new Terminal("4001", "SN-4001");
		ReflectionTestUtils.setField(stale, "id", 2L);
		ReflectionTestUtils.setField(stale, "lastSyncTime", Instant.parse("2026-04-30T10:00:00Z"));
		when(terminalRepository.findAll()).thenReturn(List.of(stale));

		List<Anomaly> created = engine.detectSyncDelay();

		assertThat(created).isNotEmpty();
		assertThat(created.get(0).getRuleCode()).isEqualTo(AnomalyRuleEngine.RULE_SYNC_DELAY);
	}

	@Test
	void rule8_flagsMissedShutdownWindow() {
		Terminal terminal = new Terminal("5100", "SN-5100");
		ReflectionTestUtils.setField(terminal, "id", 3L);
		ReflectionTestUtils.setField(terminal, "lastSyncTime", Instant.parse("2026-04-30T20:00:00Z"));
		when(terminalRepository.findAll()).thenReturn(List.of(terminal));

		List<Anomaly> created = engine.detectMissedShutdown();

		assertThat(created).isNotEmpty();
		assertThat(created.get(0).getRuleCode()).isEqualTo(AnomalyRuleEngine.RULE_MISSED_SHUTDOWN);
	}

	@Test
	void rule9_flagsCancellationWithoutMatchingSale() {
		CardTransaction cancel = new CardTransaction("C1", "A1", "SlF412349012", 1L, TransactionType.CANCELLATION, null,
				new BigDecimal("10.00"), Instant.parse("2026-05-01T10:00:05Z"), "CSV");
		when(transactionRepository.findByTransactionTimeBetween(any(), any())).thenReturn(List.of(cancel));

		List<Anomaly> created = engine.detectCancelSaleMismatch(Instant.parse("2026-05-01T00:00:00Z"),
				Instant.parse("2026-05-02T00:00:00Z"));

		assertThat(created).hasSize(1);
		assertThat(created.get(0).getRuleCode()).isEqualTo(AnomalyRuleEngine.RULE_CANCEL_SALE);
	}
}
