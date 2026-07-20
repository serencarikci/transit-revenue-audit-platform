package com.transit.audit.terminal.domain.model;

import java.time.Instant;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "terminal_depot_assignment")
public class TerminalDepotAssignment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "terminal_id", nullable = false)
	private Long terminalId;

	@Column(name = "depot_id", nullable = false)
	private Long depotId;

	@Column(name = "valid_from", nullable = false)
	private LocalDate validFrom;

	@Column(name = "valid_to")
	private LocalDate validTo;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	protected TerminalDepotAssignment() {

	}

	public TerminalDepotAssignment(Long terminalId, Long depotId, LocalDate validFrom) {
		this.terminalId = terminalId;
		this.depotId = depotId;
		this.validFrom = validFrom;
		this.createdAt = Instant.now();
	}

	public Long getId() {
		return id;
	}

	public Long getTerminalId() {
		return terminalId;
	}

	public Long getDepotId() {
		return depotId;
	}

	public LocalDate getValidFrom() {
		return validFrom;
	}

	public LocalDate getValidTo() {
		return validTo;
	}

	public void closeOn(LocalDate validTo) {
		this.validTo = validTo;
	}

	public boolean isOpen() {
		return validTo == null;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
