package com.transit.audit.reporting.application;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.transit.audit.anomaly.application.AnomalyRuleEngine;
import com.transit.audit.common.exception.BusinessException;
import com.transit.audit.common.exception.ResourceNotFoundException;
import com.transit.audit.common.util.CardAliasMasker;
import com.transit.audit.config.ReportingProperties;
import com.transit.audit.notification.application.NotificationService;
import com.transit.audit.reporting.domain.model.ReportJobStatus;
import com.transit.audit.reporting.domain.model.ReportSnapshot;
import com.transit.audit.reporting.infrastructure.persistence.ReportSnapshotRepository;
import com.transit.audit.reporting.web.request.StartReportRequest;
import com.transit.audit.reporting.web.response.ReportSnapshotResponse;
import com.transit.audit.transaction.domain.model.CardTransaction;
import com.transit.audit.transaction.infrastructure.persistence.TransactionRepository;

@Service
public class ReportingService {

	private static final Logger log = LoggerFactory.getLogger(ReportingService.class);

	private final ReportSnapshotRepository reportSnapshotRepository;
	private final TransactionRepository transactionRepository;
	private final ReportingProperties reportingProperties;
	private final AnomalyRuleEngine anomalyRuleEngine;
	private final NotificationService notificationService;
	private final ReportJobRunner reportJobRunner;

	public ReportingService(ReportSnapshotRepository reportSnapshotRepository,
			TransactionRepository transactionRepository, ReportingProperties reportingProperties,
			AnomalyRuleEngine anomalyRuleEngine, NotificationService notificationService,
			@Lazy ReportJobRunner reportJobRunner) {
		this.reportSnapshotRepository = reportSnapshotRepository;
		this.transactionRepository = transactionRepository;
		this.reportingProperties = reportingProperties;
		this.anomalyRuleEngine = anomalyRuleEngine;
		this.notificationService = notificationService;
		this.reportJobRunner = reportJobRunner;
	}

	@Transactional
	public ReportSnapshotResponse start(StartReportRequest request, String requestedBy) {
		String parametersJson = "{\"from\":\"" + request.from() + "\",\"to\":\"" + request.to()
				+ "\",\"format\":\"CSV\"}";
		ReportSnapshot snapshot = reportSnapshotRepository
				.save(new ReportSnapshot(request.reportType(), parametersJson, requestedBy));
		reportJobRunner.run(snapshot.getId(), request.from(), request.to());
		return toResponse(snapshot);
	}

	@Transactional
	public void generate(Long snapshotId, Instant from, Instant to) throws IOException {
		ReportSnapshot snapshot = reportSnapshotRepository.findById(snapshotId)
				.orElseThrow(() -> new ResourceNotFoundException("ReportSnapshot", snapshotId));

		List<CardTransaction> txs = transactionRepository.findByTransactionTimeBetween(from, to).stream()
				.sorted(Comparator.comparing(CardTransaction::getTransactionTime)).toList();
		String csv = toCsv(txs);
		String hash = ReportHashCalculator.sha256(snapshot.getParametersJson(), csv);

		reportSnapshotRepository
				.findFirstByReportTypeAndParametersJsonAndStatusOrderByCompletedAtDesc(snapshot.getReportType(),
						snapshot.getParametersJson(), ReportJobStatus.COMPLETED)
				.ifPresent(previous -> {
					if (previous.getResultHash() != null && !previous.getResultHash().equals(hash)) {
						anomalyRuleEngine.recordReportHashMismatch(snapshot.getReportType(),
								"previousHash=" + previous.getResultHash() + ", newHash=" + hash);
						notificationService
								.notifyHashMismatch("Report hash mismatch for type=" + snapshot.getReportType());
					}
				});

		Path dir = Path.of(reportingProperties.outputDir());
		Files.createDirectories(dir);
		Path out = dir.resolve("report-" + snapshotId + ".csv");
		Files.writeString(out, csv);

		snapshot.complete(hash, out.toAbsolutePath().toString());
		reportSnapshotRepository.save(snapshot);
		log.info("Report job {} completed hash={}", snapshotId, hash);
	}

	@Transactional
	public void markFailed(Long snapshotId, String message) {
		reportSnapshotRepository.findById(snapshotId).ifPresent(s -> {
			s.fail(message);
			reportSnapshotRepository.save(s);
		});
	}

	@Transactional(readOnly = true)
	public ReportSnapshotResponse get(Long id) {
		return toResponse(require(id));
	}

	@Transactional(readOnly = true)
	public Path downloadPath(Long id) {
		ReportSnapshot snapshot = require(id);
		if (snapshot.getStatus() != ReportJobStatus.COMPLETED || snapshot.getOutputPath() == null) {
			throw new BusinessException("Conflict", "Report is not ready for download", 409);
		}
		return Path.of(snapshot.getOutputPath());
	}

	private ReportSnapshot require(Long id) {
		return reportSnapshotRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("ReportSnapshot", id));
	}

	private String toCsv(List<CardTransaction> txs) {
		String header = "id,transactionReference,approvalNumber,cardAlias,terminalId,type,amount,transactionTime";
		String body = txs.stream()
				.map(tx -> String.join(",", String.valueOf(tx.getId()), tx.getTransactionReference(),
						tx.getApprovalNumber(), CardAliasMasker.mask(tx.getCardAlias()),
						String.valueOf(tx.getTerminalId()), tx.getTransactionType().name(),
						tx.getAmount().toPlainString(), tx.getTransactionTime().atZone(ZoneOffset.UTC).toString()))
				.collect(Collectors.joining("\n"));
		return body.isEmpty() ? header : header + "\n" + body;
	}

	private ReportSnapshotResponse toResponse(ReportSnapshot s) {
		return new ReportSnapshotResponse(s.getId(), s.getReportType(), s.getParametersJson(), s.getStatus(),
				s.getResultHash(), s.getOutputPath(), s.getErrorMessage(), s.getRequestedBy(), s.getStartedAt(),
				s.getCompletedAt(), s.getVersion());
	}
}
