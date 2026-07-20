package com.transit.audit.anomaly.domain.model;

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
@Table(name = "anomaly")
public class Anomaly {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "rule_code", nullable = false, length = 32)
	private String ruleCode;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 16)
	private AnomalySeverity severity;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private AnomalyStatus status;

	@Column(name = "entity_type", nullable = false, length = 64)
	private String entityType;

	@Column(name = "entity_id", nullable = false, length = 64)
	private String entityId;

	@Column(nullable = false, length = 256)
	private String title;

	@Column(columnDefinition = "TEXT")
	private String details;

	@Column(name = "detected_at", nullable = false)
	private Instant detectedAt;

	@Column(name = "reviewed_by", length = 64)
	private String reviewedBy;

	@Column(name = "resolution_note", length = 1024)
	private String resolutionNote;

	@Column(name = "resolved_at")
	private Instant resolvedAt;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@Version
	private Long version;

	protected Anomaly() {
	}

	public Anomaly(String ruleCode, AnomalySeverity severity, String entityType, String entityId, String title,
			String details) {
		this.ruleCode = ruleCode;
		this.severity = severity;
		this.status = AnomalyStatus.OPEN;
		this.entityType = entityType;
		this.entityId = entityId;
		this.title = title;
		this.details = details;
		Instant now = Instant.now();
		this.detectedAt = now;
		this.createdAt = now;
		this.updatedAt = now;
	}

	@PreUpdate
	void onUpdate() {
		this.updatedAt = Instant.now();
	}

	public void resolve(String reviewer, String resolutionNote) {
		this.status = AnomalyStatus.RESOLVED;
		this.reviewedBy = reviewer;
		this.resolutionNote = resolutionNote;
		this.resolvedAt = Instant.now();
	}

	public void markUnderReview(String reviewer) {
		this.status = AnomalyStatus.UNDER_REVIEW;
		this.reviewedBy = reviewer;
	}

	public Long getId() {
		return id;
	}

	public String getRuleCode() {
		return ruleCode;
	}

	public AnomalySeverity getSeverity() {
		return severity;
	}

	public AnomalyStatus getStatus() {
		return status;
	}

	public String getEntityType() {
		return entityType;
	}

	public String getEntityId() {
		return entityId;
	}

	public String getTitle() {
		return title;
	}

	public String getDetails() {
		return details;
	}

	public Instant getDetectedAt() {
		return detectedAt;
	}

	public String getReviewedBy() {
		return reviewedBy;
	}

	public String getResolutionNote() {
		return resolutionNote;
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
