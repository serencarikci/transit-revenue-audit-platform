package com.transit.audit.anomaly.application;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.transit.audit.anomaly.domain.model.Anomaly;
import com.transit.audit.anomaly.domain.model.AnomalySeverity;
import com.transit.audit.anomaly.domain.model.AnomalyStatus;
import com.transit.audit.anomaly.infrastructure.persistence.AnomalyRepository;
import com.transit.audit.common.util.CardAliasMasker;
import com.transit.audit.config.AnomalyProperties;
import com.transit.audit.config.PdaProperties;
import com.transit.audit.notification.application.NotificationService;
import com.transit.audit.terminal.domain.model.Terminal;
import com.transit.audit.terminal.infrastructure.persistence.TerminalRepository;
import com.transit.audit.transaction.domain.model.CardTransaction;
import com.transit.audit.transaction.domain.model.TransactionType;
import com.transit.audit.transaction.infrastructure.persistence.TransactionRepository;

@Service
public class AnomalyRuleEngine {

	public static final String RULE_SYNC_DELAY = "RULE_7_SYNC_DELAY";
	public static final String RULE_MISSED_SHUTDOWN = "RULE_8_MISSED_SHUTDOWN";
	public static final String RULE_CANCEL_SALE = "RULE_9_CANCEL_SALE";
	public static final String RULE_REPORT_HASH = "RULE_11_REPORT_HASH";

	private static final Logger log = LoggerFactory.getLogger(AnomalyRuleEngine.class);

	private final AnomalyRepository anomalyRepository;
	private final TerminalRepository terminalRepository;
	private final TransactionRepository transactionRepository;
	private final PdaProperties pdaProperties;
	private final AnomalyProperties anomalyProperties;
	private final NotificationService notificationService;
	private final Clock clock;

	public AnomalyRuleEngine(AnomalyRepository anomalyRepository, TerminalRepository terminalRepository,
			TransactionRepository transactionRepository, PdaProperties pdaProperties,
			AnomalyProperties anomalyProperties, NotificationService notificationService, Clock clock) {
		this.anomalyRepository = anomalyRepository;
		this.terminalRepository = terminalRepository;
		this.transactionRepository = transactionRepository;
		this.pdaProperties = pdaProperties;
		this.anomalyProperties = anomalyProperties;
		this.notificationService = notificationService;
		this.clock = clock;
	}

	@Transactional
	public List<Anomaly> runPdaScan() {
		List<Anomaly> created = new ArrayList<>();
		created.addAll(detectSyncDelay());
		created.addAll(detectMissedShutdown());
		return created;
	}

	@Transactional
	public List<Anomaly> runCancelSaleMatching(Instant from, Instant to) {
		return detectCancelSaleMismatch(from, to);
	}


	List<Anomaly> detectSyncDelay() {
		Instant threshold = clock.instant().minus(Duration.ofHours(pdaProperties.syncDelayHours()));
		List<Anomaly> created = new ArrayList<>();
		for (Terminal terminal : terminalRepository.findAll()) {
			if (!terminal.isActive()) {
				continue;
			}
			Instant lastSync = terminal.getLastSyncTime();
			if (lastSync == null || lastSync.isBefore(threshold)) {
				created.add(
						saveIfNew(RULE_SYNC_DELAY, AnomalySeverity.HIGH, "Terminal", String.valueOf(terminal.getId()),
								"Terminal sync delayed beyond " + pdaProperties.syncDelayHours() + "h",
								"terminalNumber=" + terminal.getTerminalNumber() + ", lastSyncTime=" + lastSync));
			}
		}
		return created;
	}



	List<Anomaly> detectMissedShutdown() {
		LocalDate today = LocalDate.ofInstant(clock.instant(), ZoneOffset.UTC);
		LocalDateTime planned = today.minusDays(1).atTime(pdaProperties.plannedShutdownHour(), 0);
		Instant plannedInstant = planned.toInstant(ZoneOffset.UTC);
		Instant windowEnd = planned.plusHours(2).toInstant(ZoneOffset.UTC);

		List<Anomaly> created = new ArrayList<>();
		for (Terminal terminal : terminalRepository.findAll()) {
			if (!terminal.isActive()) {
				continue;
			}
			Instant lastSync = terminal.getLastSyncTime();
			boolean syncedInWindow = lastSync != null && !lastSync.isBefore(plannedInstant)
					&& !lastSync.isAfter(windowEnd);
			if (!syncedInWindow) {
				created.add(saveIfNew(RULE_MISSED_SHUTDOWN, AnomalySeverity.MEDIUM, "Terminal",
						String.valueOf(terminal.getId()), "Terminal missed planned shutdown sync",
						"terminalNumber=" + terminal.getTerminalNumber() + ", planned=" + plannedInstant
								+ ", lastSyncTime=" + lastSync));
			}
		}
		return created;
	}



	List<Anomaly> detectCancelSaleMismatch(Instant from, Instant to) {
		Duration proximity = Duration.ofSeconds(anomalyProperties.cancelSaleProximitySeconds());
		List<CardTransaction> txs = transactionRepository.findByTransactionTimeBetween(from, to);
		List<Anomaly> created = new ArrayList<>();

		List<CardTransaction> cancellations = txs.stream()
				.filter(t -> t.getTransactionType() == TransactionType.CANCELLATION).toList();
		List<CardTransaction> sales = txs.stream().filter(t -> t.getTransactionType() == TransactionType.SALE).toList();

		for (CardTransaction cancel : cancellations) {
			boolean matched = sales.stream()
					.anyMatch(sale -> sale.getTerminalId().equals(cancel.getTerminalId())
							&& sale.getCardAlias().equals(cancel.getCardAlias())
							&& sale.getAmount().compareTo(cancel.getAmount()) == 0
							&& Duration.between(sale.getTransactionTime(), cancel.getTransactionTime()).abs()
									.compareTo(proximity) <= 0);
			if (!matched) {
				created.add(saveIfNew(RULE_CANCEL_SALE, AnomalySeverity.HIGH, "CardTransaction",
						String.valueOf(cancel.getId()), "Cancellation without matching sale",
						"cardAlias=" + CardAliasMasker.mask(cancel.getCardAlias()) + ", terminalId="
								+ cancel.getTerminalId() + ", amount=" + cancel.getAmount()));
			}
		}
		return created;
	}

	@Transactional
	public Anomaly recordReportHashMismatch(String reportType, String details) {
		return saveIfNew(RULE_REPORT_HASH, AnomalySeverity.CRITICAL, "ReportSnapshot", reportType,
				"Report result hash mismatch for identical parameters", details);
	}

	private Anomaly saveIfNew(String ruleCode, AnomalySeverity severity, String entityType, String entityId,
			String title, String details) {
		if (anomalyRepository.existsByRuleCodeAndEntityTypeAndEntityIdAndStatus(ruleCode, entityType, entityId,
				AnomalyStatus.OPEN)) {
			return null;
		}
		Anomaly anomaly = anomalyRepository.save(new Anomaly(ruleCode, severity, entityType, entityId, title, details));
		log.info("Anomaly created rule={} entity={}:{} severity={}", ruleCode, entityType, entityId, severity);
		notificationService.notifyAnomaly(anomaly);
		return anomaly;
	}
}
