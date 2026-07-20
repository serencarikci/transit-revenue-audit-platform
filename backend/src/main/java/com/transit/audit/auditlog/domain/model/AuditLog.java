package com.transit.audit.auditlog.domain.model;

import java.time.Instant;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "audit_log")
public class AuditLog {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 64)
	private String action;

	@Column(name = "entity_type", nullable = false, length = 64)
	private String entityType;

	@Column(name = "entity_id", length = 64)
	private String entityId;

	@Column(name = "actor_username", length = 64)
	private String actorUsername;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "details_json")
	private String detailsJson;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	protected AuditLog() {
	}

	public AuditLog(String action, String entityType, String entityId, String actorUsername, String detailsJson) {
		this.action = action;
		this.entityType = entityType;
		this.entityId = entityId;
		this.actorUsername = actorUsername;
		this.detailsJson = detailsJson;
		this.createdAt = Instant.now();
	}

	public Long getId() {
		return id;
	}

	public String getAction() {
		return action;
	}

	public String getEntityType() {
		return entityType;
	}

	public String getEntityId() {
		return entityId;
	}

	public String getActorUsername() {
		return actorUsername;
	}

	public String getDetailsJson() {
		return detailsJson;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
