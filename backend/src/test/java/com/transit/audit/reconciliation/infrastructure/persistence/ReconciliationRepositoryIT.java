package com.transit.audit.reconciliation.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import com.transit.audit.auditlog.infrastructure.persistence.AuditLogRepository;
import com.transit.audit.depot.domain.model.Depot;
import com.transit.audit.depot.infrastructure.persistence.DepotRepository;
import com.transit.audit.reconciliation.application.ReconciliationService;
import com.transit.audit.reconciliation.domain.model.FinancialPeriodStatus;
import com.transit.audit.reconciliation.domain.model.ReconciliationStatus;
import com.transit.audit.reconciliation.web.request.CreateFinancialPeriodRequest;
import com.transit.audit.reconciliation.web.request.ResolveReconciliationRequest;
import com.transit.audit.reconciliation.web.response.FinancialPeriodResponse;
import com.transit.audit.reconciliation.web.response.ReconciliationResultResponse;
import com.transit.audit.support.AbstractPostgresIntegrationTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
class ReconciliationRepositoryIT extends AbstractPostgresIntegrationTest {

	@Autowired
	private DepotRepository depotRepository;

	@Autowired
	private FinancialPeriodRepository financialPeriodRepository;

	@Autowired
	private ReconciliationResultRepository reconciliationResultRepository;

	@Autowired
	private ReconciliationService reconciliationService;

	@Autowired
	private AuditLogRepository auditLogRepository;

	@Test
	void calculate_persistsMatchedStatus_andWritesAuditLog() {
		Depot depot = depotRepository.save(new Depot("R90", "Reconcile Depot"));
		FinancialPeriodResponse period = reconciliationService.createPeriod(new CreateFinancialPeriodRequest(
				depot.getId(), LocalDate.of(2026, 5, 1), new BigDecimal("100.00"), new BigDecimal("50.00"),
				new BigDecimal("20.00"), new BigDecimal("130.00")));

		ReconciliationResultResponse result = reconciliationService.calculate(period.id());

		assertThat(result.expectedClosingBalance()).isEqualByComparingTo("130.00");
		assertThat(result.variance()).isEqualByComparingTo("0.00");
		assertThat(result.status()).isEqualTo(ReconciliationStatus.MATCHED);
		assertThat(financialPeriodRepository.findById(period.id())).get()
				.extracting(p -> p.getStatus()).isEqualTo(FinancialPeriodStatus.RECONCILED);
		assertThat(auditLogRepository.search("ReconciliationResult", null)).isNotEmpty().anySatisfy(log -> {
			assertThat(log.getAction()).isEqualTo("RECONCILIATION_CALCULATE");
			assertThat(log.getEntityId()).isEqualTo(String.valueOf(result.id()));
		});
	}

	@Test
	void calculate_classifiesLargeVariance_andSupportsYearMonthFilter() {
		Depot depot = depotRepository.save(new Depot("R91", "Variance Depot"));
		FinancialPeriodResponse period = reconciliationService.createPeriod(new CreateFinancialPeriodRequest(
				depot.getId(), LocalDate.of(2026, 7, 15), new BigDecimal("100.00"), new BigDecimal("50.00"),
				new BigDecimal("20.00"), new BigDecimal("120.00")));

		ReconciliationResultResponse result = reconciliationService.calculate(period.id());

		assertThat(result.variance()).isEqualByComparingTo("-10.00");
		assertThat(result.status()).isEqualTo(ReconciliationStatus.LARGE_VARIANCE);
		assertThat(reconciliationResultRepository.search(2026, 7, ReconciliationStatus.LARGE_VARIANCE, PageRequest.of(0, 10))
				.getContent()).extracting(r -> r.getId()).contains(result.id());
		assertThat(reconciliationResultRepository.search(2025, null, null, PageRequest.of(0, 10))).isEmpty();
	}

	@Test
	void resolve_marksLargeVarianceAsResolved() {
		Depot depot = depotRepository.save(new Depot("R92", "Resolve Depot"));
		FinancialPeriodResponse period = reconciliationService.createPeriod(new CreateFinancialPeriodRequest(
				depot.getId(), LocalDate.of(2026, 8, 1), new BigDecimal("100.00"), new BigDecimal("10.00"),
				new BigDecimal("0.00"), new BigDecimal("105.00")));
		ReconciliationResultResponse calculated = reconciliationService.calculate(period.id());
		assertThat(calculated.status()).isEqualTo(ReconciliationStatus.LARGE_VARIANCE);

		ReconciliationResultResponse resolved = reconciliationService.resolve(calculated.id(),
				new ResolveReconciliationRequest("Cash count adjusted", calculated.version()), "finance");

		assertThat(resolved.status()).isEqualTo(ReconciliationStatus.RESOLVED);
		assertThat(resolved.resolvedBy()).isEqualTo("finance");
		assertThat(resolved.resolutionNote()).isEqualTo("Cash count adjusted");
	}
}
