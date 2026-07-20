package com.transit.audit.reconciliation.web;

import java.time.Instant;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.transit.audit.common.web.IfMatchSupport;
import com.transit.audit.reconciliation.application.ReconciliationService;
import com.transit.audit.reconciliation.domain.model.ReconciliationStatus;
import com.transit.audit.reconciliation.web.request.CreateFinancialPeriodRequest;
import com.transit.audit.reconciliation.web.request.ResolveReconciliationRequest;
import com.transit.audit.reconciliation.web.response.FinancialPeriodResponse;
import com.transit.audit.reconciliation.web.response.ReconciliationResultResponse;
import com.transit.audit.reconciliation.web.response.UnassignedTransactionResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/reconciliation")
@Tag(name = "Reconciliation", description = "Financial period creation, variance calculation and resolution")
public class ReconciliationController {

	private final ReconciliationService reconciliationService;

	public ReconciliationController(ReconciliationService reconciliationService) {
		this.reconciliationService = reconciliationService;
	}

	@PostMapping("/periods")
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("hasAnyRole('ADMIN','FINANCE_USER')")
	@Operation(summary = "Create a financial period")
	public FinancialPeriodResponse createPeriod(@Valid @RequestBody CreateFinancialPeriodRequest request) {
		return reconciliationService.createPeriod(request);
	}

	@GetMapping("/periods/{id}")
	@PreAuthorize("isAuthenticated()")
	@Operation(summary = "Get a financial period by id")
	public FinancialPeriodResponse getPeriod(@PathVariable Long id) {
		return reconciliationService.getPeriod(id);
	}

	@PostMapping("/periods/{id}/calculate")
	@PreAuthorize("hasAnyRole('ADMIN','FINANCE_USER')")
	@Operation(summary = "Calculate expected closing balance and variance for a period")
	public ReconciliationResultResponse calculate(@PathVariable Long id) {
		return reconciliationService.calculate(id);
	}

	@PostMapping("/periods/{id}/run")
	@PreAuthorize("hasAnyRole('ADMIN','FINANCE_USER')")
	@Operation(summary = "Alias of calculate", deprecated = true)
	public ReconciliationResultResponse run(@PathVariable Long id) {
		return reconciliationService.calculate(id);
	}

	@GetMapping("/results")
	@PreAuthorize("isAuthenticated()")
	@Operation(summary = "List reconciliation results with optional year, month and status filters")
	public PagedModel<ReconciliationResultResponse> listResults(@RequestParam(required = false) Integer year,
			@RequestParam(required = false) Integer month, @RequestParam(required = false) ReconciliationStatus status,
			Pageable pageable) {
		return new PagedModel<>(reconciliationService.listResults(year, month, status, pageable));
	}

	@PostMapping("/results/{id}/resolve")
	@PreAuthorize("hasAnyRole('ADMIN','FINANCE_USER')")
	@Operation(summary = "Resolve a small or large variance")
	public ReconciliationResultResponse resolve(@PathVariable Long id,
			@Valid @RequestBody ResolveReconciliationRequest request,
			@RequestHeader(value = "If-Match", required = false) String ifMatch, Authentication authentication) {
		IfMatchSupport.requireMatchesBodyVersion(ifMatch, request.version());
		return reconciliationService.resolve(id, request, authentication.getName());
	}

	@GetMapping("/unassigned-transactions")
	@PreAuthorize("hasAnyRole('ADMIN','FINANCE_USER','AUDITOR')")
	@Operation(summary = "List transactions without a covering depot assignment")
	public List<UnassignedTransactionResponse> unassigned(@RequestParam Instant from, @RequestParam Instant to) {
		return reconciliationService.findTransactionsWithoutDepotAssignment(from, to);
	}
}
