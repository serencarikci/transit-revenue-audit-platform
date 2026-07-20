package com.transit.audit.reconciliation.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

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
@Table(name = "financial_period")
public class FinancialPeriod {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "depot_id", nullable = false)
	private Long depotId;

	@Column(name = "period_date", nullable = false)
	private LocalDate periodDate;

	@Column(name = "opening_balance", nullable = false, precision = 19, scale = 2)
	private BigDecimal openingBalance;

	@Column(name = "deposited_amount", nullable = false, precision = 19, scale = 2)
	private BigDecimal depositedAmount;

	@Column(name = "withdrawal_amount", nullable = false, precision = 19, scale = 2)
	private BigDecimal withdrawalAmount;

	@Column(name = "actual_closing_balance", precision = 19, scale = 2)
	private BigDecimal actualClosingBalance;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private FinancialPeriodStatus status;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@Version
	private Long version;

	protected FinancialPeriod() {
	}

	public FinancialPeriod(Long depotId, LocalDate periodDate, BigDecimal openingBalance, BigDecimal depositedAmount,
			BigDecimal withdrawalAmount, BigDecimal actualClosingBalance) {
		this.depotId = depotId;
		this.periodDate = periodDate;
		this.openingBalance = openingBalance;
		this.depositedAmount = depositedAmount;
		this.withdrawalAmount = withdrawalAmount;
		this.actualClosingBalance = actualClosingBalance;
		this.status = FinancialPeriodStatus.OPEN;
		Instant now = Instant.now();
		this.createdAt = now;
		this.updatedAt = now;
	}

	@PreUpdate
	void onUpdate() {
		this.updatedAt = Instant.now();
	}

	public Long getId() {
		return id;
	}

	public Long getDepotId() {
		return depotId;
	}

	public LocalDate getPeriodDate() {
		return periodDate;
	}

	public BigDecimal getOpeningBalance() {
		return openingBalance;
	}

	public BigDecimal getDepositedAmount() {
		return depositedAmount;
	}

	public BigDecimal getWithdrawalAmount() {
		return withdrawalAmount;
	}

	public BigDecimal getActualClosingBalance() {
		return actualClosingBalance;
	}

	public void setActualClosingBalance(BigDecimal actualClosingBalance) {
		this.actualClosingBalance = actualClosingBalance;
	}

	public FinancialPeriodStatus getStatus() {
		return status;
	}

	public void setStatus(FinancialPeriodStatus status) {
		this.status = status;
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
