package com.transit.audit.reporting.domain.model;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "report_snapshot")
public class ReportSnapshot {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "report_type", nullable = false, length = 64)
	private String reportType;

	@Column(name = "parameters_json", nullable = false, columnDefinition = "TEXT")
	private String parametersJson;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private ReportJobStatus status;

	@Column(name = "result_hash", length = 128)
	private String resultHash;

	@Column(name = "output_path", length = 512)
	private String outputPath;

	@Column(name = "error_message", columnDefinition = "TEXT")
	private String errorMessage;

	@Column(name = "requested_by", length = 64)
	private String requestedBy;

	@Column(name = "started_at", nullable = false)
	private Instant startedAt;

	@Column(name = "completed_at")
	private Instant completedAt;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Version
	private Long version;

	protected ReportSnapshot() {
	}

	public ReportSnapshot(String reportType, String parametersJson, String requestedBy) {
		this.reportType = reportType;
		this.parametersJson = parametersJson;
		this.requestedBy = requestedBy;
		this.status = ReportJobStatus.RUNNING;
		Instant now = Instant.now();
		this.startedAt = now;
		this.createdAt = now;
	}

	public void complete(String resultHash, String outputPath) {
		this.status = ReportJobStatus.COMPLETED;
		this.resultHash = resultHash;
		this.outputPath = outputPath;
		this.completedAt = Instant.now();
	}

	public void fail(String errorMessage) {
		this.status = ReportJobStatus.FAILED;
		this.errorMessage = errorMessage;
		this.completedAt = Instant.now();
	}

	public Long getId() {
		return id;
	}

	public String getReportType() {
		return reportType;
	}

	public String getParametersJson() {
		return parametersJson;
	}

	public ReportJobStatus getStatus() {
		return status;
	}

	public String getResultHash() {
		return resultHash;
	}

	public String getOutputPath() {
		return outputPath;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public String getRequestedBy() {
		return requestedBy;
	}

	public Instant getStartedAt() {
		return startedAt;
	}

	public Instant getCompletedAt() {
		return completedAt;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Long getVersion() {
		return version;
	}
}
