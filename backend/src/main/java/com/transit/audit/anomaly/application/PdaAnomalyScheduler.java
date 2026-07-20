package com.transit.audit.anomaly.application;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PdaAnomalyScheduler {

	private static final Logger log = LoggerFactory.getLogger(PdaAnomalyScheduler.class);

	private final AnomalyRuleEngine anomalyRuleEngine;

	public PdaAnomalyScheduler(AnomalyRuleEngine anomalyRuleEngine) {
		this.anomalyRuleEngine = anomalyRuleEngine;
	}

	@Scheduled(cron = "${app.pda.scan-cron}")
	public void scanTerminals() {
		log.info("Running PDA anomaly scan (rules 7 & 8)");
		var created = anomalyRuleEngine.runPdaScan().stream().filter(Objects::nonNull).toList();
		log.info("PDA anomaly scan created {} anomalies", created.size());
	}

	@Scheduled(cron = "${app.anomaly.scan-cron}")
	public void scanCancelSale() {
		Instant to = Instant.now();
		Instant from = to.minus(1, ChronoUnit.DAYS);
		log.info("Running cancel-sale anomaly scan (rule 9) from {} to {}", from, to);
		List<?> created = anomalyRuleEngine.runCancelSaleMatching(from, to).stream().filter(Objects::nonNull).toList();
		log.info("Cancel-sale scan created {} anomalies", created.size());
	}
}
