package com.transit.audit.reconciliation.domain.model;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "reconciliation_result")
public class ReconciliationResult {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "financial_period_id", nullable = false)
	private Long financialPeriodId;

	@Column(name = "expected_closing_balance", nullable = false, precision = 19, scale = 2)
	private BigDecimal expectedClosingBalance;

	@Column(name = "actual_closing_balance", nullable = false, precision = 19, scale = 2)
	private BigDecimal actualClosingBalance;

	@Column(nullable = false, precision = 19, scale = 2)
	private BigDecimal variance;

	@Column(name = "sale_amount", nullable = false, precision = 19, scale = 2)
	private BigDecimal saleAmount;

	@Column(name = "cancellation_amount", nullable = false, precision = 19, scale = 2)
	private BigDecimal cancellationAmount;

	@Column(name = "net_amount", nullable = false, precision = 19, scale = 2)
	private BigDecimal netAmount;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private ReconciliationStatus status;

	@Column(name = "resolution_note", length = 1024)
	private String resolutionNote;

	@Column(name = "resolved_by", length = 64)
	private String resolvedBy;

	@Column(name = "resolved_at")
	private Instant resolvedAt;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@Version
	private Long version;

	protected ReconciliationResult() {
	}

	public ReconciliationResult(Long financialPeriodId, BigDecimal expectedClosingBalance,
			BigDecimal actualClosingBalance, BigDecimal variance, BigDecimal saleAmount, BigDecimal cancellationAmount,
			BigDecimal netAmount, ReconciliationStatus status) {
		this.financialPeriodId = financialPeriodId;
		this.expectedClosingBalance = expectedClosingBalance;
		this.actualClosingBalance = actualClosingBalance;
		this.variance = variance;
		this.saleAmount = saleAmount;
		this.cancellationAmount = cancellationAmount;
		this.netAmount = netAmount;
		this.status = status;
		Instant now = Instant.now();
		this.createdAt = now;
		this.updatedAt = now;
	}

	@PreUpdate
	void onUpdate() {
		this.updatedAt = Instant.now();
	}

	public void resolve(String resolvedBy, String resolutionNote) {
		this.status = ReconciliationStatus.RESOLVED;
		this.resolvedBy = resolvedBy;
		this.resolutionNote = resolutionNote;
		this.resolvedAt = Instant.now();
	}

	public Long getId() {
		return id;
	}

	public Long getFinancialPeriodId() {
		return financialPeriodId;
	}

	public BigDecimal getExpectedClosingBalance() {
		return expectedClosingBalance;
	}

	public BigDecimal getActualClosingBalance() {
		return actualClosingBalance;
	}

	public BigDecimal getVariance() {
		return variance;
	}

	public BigDecimal getSaleAmount() {
		return saleAmount;
	}

	public BigDecimal getCancellationAmount() {
		return cancellationAmount;
	}

	public BigDecimal getNetAmount() {
		return netAmount;
	}

	public ReconciliationStatus getStatus() {
		return status;
	}

	public String getResolutionNote() {
		return resolutionNote;
	}

	public String getResolvedBy() {
		return resolvedBy;
	}

	public Instant getResolvedAt() {
		return resolvedAt;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public Long getVersion() {
		return version;
	}
}
