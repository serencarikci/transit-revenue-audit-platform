package com.transit.audit.transaction.domain.model;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "card_transaction")
public class CardTransaction {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "transaction_reference", nullable = false, unique = true, length = 64)
	private String transactionReference;

	@Column(name = "approval_number", nullable = false, length = 64)
	private String approvalNumber;

	@Column(name = "card_alias", nullable = false, length = 64)
	private String cardAlias;

	@Column(name = "terminal_id", nullable = false)
	private Long terminalId;

	@Enumerated(EnumType.STRING)
	@Column(name = "transaction_type", nullable = false, length = 32)
	private TransactionType transactionType;

	@Column(name = "product_type", length = 64)
	private String productType;

	@Column(nullable = false, precision = 19, scale = 2)
	private BigDecimal amount;

	@Column(name = "transaction_time", nullable = false)
	private Instant transactionTime;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "source_system", nullable = false, length = 64)
	private String sourceSystem;

	protected CardTransaction() {

	}

	public CardTransaction(String transactionReference, String approvalNumber, String cardAlias, Long terminalId,
			TransactionType transactionType, String productType, BigDecimal amount, Instant transactionTime,
			String sourceSystem) {
		this.transactionReference = transactionReference;
		this.approvalNumber = approvalNumber;
		this.cardAlias = cardAlias;
		this.terminalId = terminalId;
		this.transactionType = transactionType;
		this.productType = productType;
		this.amount = amount;
		this.transactionTime = transactionTime;
		this.sourceSystem = sourceSystem;
		this.createdAt = Instant.now();
	}

	public Long getId() {
		return id;
	}

	public String getTransactionReference() {
		return transactionReference;
	}

	public String getApprovalNumber() {
		return approvalNumber;
	}

	public String getCardAlias() {
		return cardAlias;
	}

	public Long getTerminalId() {
		return terminalId;
	}

	public TransactionType getTransactionType() {
		return transactionType;
	}

	public String getProductType() {
		return productType;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public Instant getTransactionTime() {
		return transactionTime;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public String getSourceSystem() {
		return sourceSystem;
	}
}
