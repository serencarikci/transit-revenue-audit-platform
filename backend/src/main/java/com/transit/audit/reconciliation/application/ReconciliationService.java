package com.transit.audit.reconciliation.application;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.transit.audit.common.concurrency.OptimisticLockSupport;
import com.transit.audit.common.exception.BusinessException;
import com.transit.audit.common.exception.ResourceNotFoundException;
import com.transit.audit.config.ReconciliationProperties;
import com.transit.audit.depot.infrastructure.persistence.DepotRepository;
import com.transit.audit.reconciliation.domain.model.FinancialPeriod;
import com.transit.audit.reconciliation.domain.model.FinancialPeriodStatus;
import com.transit.audit.reconciliation.domain.model.ReconciliationResult;
import com.transit.audit.reconciliation.domain.model.ReconciliationStatus;
import com.transit.audit.reconciliation.infrastructure.persistence.FinancialPeriodRepository;
import com.transit.audit.reconciliation.infrastructure.persistence.ReconciliationResultRepository;
import com.transit.audit.reconciliation.web.request.CreateFinancialPeriodRequest;
import com.transit.audit.reconciliation.web.request.ResolveReconciliationRequest;
import com.transit.audit.reconciliation.web.response.FinancialPeriodResponse;
import com.transit.audit.reconciliation.web.response.ReconciliationResultResponse;
import com.transit.audit.reconciliation.web.response.UnassignedTransactionResponse;
import com.transit.audit.terminal.application.TerminalService;
import com.transit.audit.transaction.domain.model.CardTransaction;
import com.transit.audit.transaction.domain.model.TransactionType;
import com.transit.audit.transaction.infrastructure.persistence.TransactionRepository;

@Service
public class ReconciliationService {

	private final FinancialPeriodRepository financialPeriodRepository;
	private final ReconciliationResultRepository reconciliationResultRepository;
	private final DepotRepository depotRepository;
	private final TransactionRepository transactionRepository;
	private final TerminalService terminalService;
	private final ReconciliationProperties reconciliationProperties;

	public ReconciliationService(FinancialPeriodRepository financialPeriodRepository,
			ReconciliationResultRepository reconciliationResultRepository, DepotRepository depotRepository,
			TransactionRepository transactionRepository, TerminalService terminalService,
			ReconciliationProperties reconciliationProperties) {
		this.financialPeriodRepository = financialPeriodRepository;
		this.reconciliationResultRepository = reconciliationResultRepository;
		this.depotRepository = depotRepository;
		this.transactionRepository = transactionRepository;
		this.terminalService = terminalService;
		this.reconciliationProperties = reconciliationProperties;
	}

	@Transactional
	public FinancialPeriodResponse createPeriod(CreateFinancialPeriodRequest request) {
		if (!depotRepository.existsById(request.depotId())) {
			throw new ResourceNotFoundException("Depot", request.depotId());
		}
		if (financialPeriodRepository.existsByDepotIdAndPeriodDate(request.depotId(), request.periodDate())) {
			throw new BusinessException("Conflict", "Financial period already exists for this depot and date", 409);
		}
		FinancialPeriod period = new FinancialPeriod(request.depotId(), request.periodDate(), request.openingBalance(),
				request.depositedAmount(), request.withdrawalAmount(), request.actualClosingBalance());
		return toPeriodResponse(financialPeriodRepository.save(period));
	}

	@Transactional(readOnly = true)
	public FinancialPeriodResponse getPeriod(Long id) {
		return toPeriodResponse(requirePeriod(id));
	}

	@Transactional
	public ReconciliationResultResponse calculate(Long periodId) {
		FinancialPeriod period = requirePeriod(periodId);
		BigDecimal expected = ExpectedClosingBalanceCalculator.calculate(period.getOpeningBalance(),
				period.getDepositedAmount(), period.getWithdrawalAmount());
		BigDecimal actual = period.getActualClosingBalance() == null ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
				: period.getActualClosingBalance().setScale(2, RoundingMode.HALF_UP);
		BigDecimal variance = VarianceClassifier.variance(actual, expected);
		ReconciliationStatus status = VarianceClassifier.classify(variance,
				reconciliationProperties.smallVarianceThreshold());

		PeriodTotals totals = computeTotalsForDepotDay(period.getDepotId(), period.getPeriodDate());

		ReconciliationResult result = new ReconciliationResult(period.getId(), expected, actual, variance,
				totals.saleAmount(), totals.cancellationAmount(), totals.netAmount(), status);
		ReconciliationResult saved = reconciliationResultRepository.save(result);

		if (status == ReconciliationStatus.MATCHED) {
			period.setStatus(FinancialPeriodStatus.RECONCILED);
		}
		financialPeriodRepository.save(period);
		return toResultResponse(saved);
	}

	@Transactional(readOnly = true)
	public Page<ReconciliationResultResponse> listResults(Integer year, Integer month, ReconciliationStatus status,
			Pageable pageable) {
		if (month != null && (month < 1 || month > 12)) {
			throw new BusinessException("Validation", "month must be between 1 and 12", 400);
		}
		return reconciliationResultRepository.search(year, month, status, pageable).map(this::toResultResponse);
	}

	@Transactional
	public ReconciliationResultResponse resolve(Long id, ResolveReconciliationRequest request, String actor) {
		ReconciliationResult result = reconciliationResultRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("ReconciliationResult", id));
		OptimisticLockSupport.requireVersion(result.getVersion(), request.version(), "Reconciliation result");
		if (result.getStatus() == ReconciliationStatus.MATCHED) {
			throw new BusinessException("Conflict", "Matched reconciliation cannot be resolved", 409);
		}
		if (result.getStatus() == ReconciliationStatus.RESOLVED) {
			throw new BusinessException("Conflict", "Reconciliation already resolved", 409);
		}
		result.resolve(actor, request.resolutionNote());
		return toResultResponse(reconciliationResultRepository.save(result));
	}

	@Transactional(readOnly = true)
	public List<UnassignedTransactionResponse> findTransactionsWithoutDepotAssignment(Instant from, Instant to) {
		List<UnassignedTransactionResponse> unmatched = new ArrayList<>();
		for (CardTransaction tx : transactionRepository.findByTransactionTimeBetween(from, to)) {
			if (terminalService.findDepotForTerminalAt(tx.getTerminalId(), tx.getTransactionTime()).isEmpty()) {
				unmatched.add(new UnassignedTransactionResponse(tx.getId(), tx.getTerminalId(),
						tx.getTransactionReference(), tx.getTransactionTime().toString()));
			}
		}
		return unmatched;
	}

	private PeriodTotals computeTotalsForDepotDay(Long depotId, LocalDate periodDate) {
		Instant from = periodDate.atStartOfDay().toInstant(ZoneOffset.UTC);
		Instant to = periodDate.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC).minusNanos(1);
		BigDecimal sale = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
		BigDecimal cancellation = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

		for (CardTransaction tx : transactionRepository.findByTransactionTimeBetween(from, to)) {
			var depot = terminalService.findDepotForTerminalAt(tx.getTerminalId(), tx.getTransactionTime());
			if (depot.isEmpty() || !depot.get().getId().equals(depotId)) {
				continue;
			}
			if (tx.getTransactionType() == TransactionType.SALE) {
				sale = sale.add(tx.getAmount());
			} else if (tx.getTransactionType() == TransactionType.CANCELLATION) {
				cancellation = cancellation.add(tx.getAmount());
			}
		}
		BigDecimal net = sale.subtract(cancellation);
		return new PeriodTotals(sale, cancellation, net);
	}

	private FinancialPeriod requirePeriod(Long id) {
		return financialPeriodRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("FinancialPeriod", id));
	}

	private FinancialPeriodResponse toPeriodResponse(FinancialPeriod p) {
		return new FinancialPeriodResponse(p.getId(), p.getDepotId(), p.getPeriodDate(), p.getOpeningBalance(),
				p.getDepositedAmount(), p.getWithdrawalAmount(), p.getActualClosingBalance(), p.getStatus(),
				p.getCreatedAt(), p.getUpdatedAt(), p.getVersion());
	}

	private ReconciliationResultResponse toResultResponse(ReconciliationResult r) {
		return new ReconciliationResultResponse(r.getId(), r.getFinancialPeriodId(), r.getExpectedClosingBalance(),
				r.getActualClosingBalance(), r.getVariance(), r.getSaleAmount(), r.getCancellationAmount(),
				r.getNetAmount(), r.getStatus(), r.getResolutionNote(), r.getResolvedBy(), r.getResolvedAt(),
				r.getCreatedAt(), r.getVersion());
	}

	private record PeriodTotals(BigDecimal saleAmount, BigDecimal cancellationAmount, BigDecimal netAmount) {
	}
}
