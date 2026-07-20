package com.transit.audit.terminal.domain.model;

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
@Table(name = "terminal")
public class Terminal {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "terminal_number", nullable = false, unique = true, length = 32)
	private String terminalNumber;

	@Column(name = "serial_number", nullable = false, unique = true, length = 64)
	private String serialNumber;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private TerminalStatus status;

	@Column(name = "last_sync_time")
	private Instant lastSyncTime;

	@Column(name = "last_transaction_time")
	private Instant lastTransactionTime;

	@Column(name = "pending_transaction_count", nullable = false)
	private int pendingTransactionCount;

	@Column(name = "retry_count", nullable = false)
	private int retryCount;

	@Column(nullable = false)
	private boolean active;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@Version
	private Long version;

	protected Terminal() {

	}

	public Terminal(String terminalNumber, String serialNumber) {
		this.terminalNumber = terminalNumber;
		this.serialNumber = serialNumber;
		this.status = TerminalStatus.HEALTHY;
		this.pendingTransactionCount = 0;
		this.retryCount = 0;
		this.active = true;
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

	public String getTerminalNumber() {
		return terminalNumber;
	}

	public String getSerialNumber() {
		return serialNumber;
	}

	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}

	public TerminalStatus getStatus() {
		return status;
	}

	public void setStatus(TerminalStatus status) {
		this.status = status;
	}

	public Instant getLastSyncTime() {
		return lastSyncTime;
	}

	public Instant getLastTransactionTime() {
		return lastTransactionTime;
	}

	public int getPendingTransactionCount() {
		return pendingTransactionCount;
	}

	public int getRetryCount() {
		return retryCount;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
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
