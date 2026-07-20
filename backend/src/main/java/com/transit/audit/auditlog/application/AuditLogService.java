package com.transit.audit.auditlog.application;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.transit.audit.auditlog.domain.model.AuditLog;
import com.transit.audit.auditlog.infrastructure.persistence.AuditLogRepository;
import com.transit.audit.auditlog.web.response.AuditLogResponse;

@Service
public class AuditLogService {

	private final AuditLogRepository auditLogRepository;

	public AuditLogService(AuditLogRepository auditLogRepository) {
		this.auditLogRepository = auditLogRepository;
	}

	@Transactional
	public void record(String action, String entityType, String entityId, String actorUsername, String detailsJson) {
		auditLogRepository.save(new AuditLog(action, entityType, entityId, actorUsername, detailsJson));
	}

	@Transactional(readOnly = true)
	public Page<AuditLogResponse> list(Pageable pageable) {
		return auditLogRepository.findAll(pageable).map(this::toResponse);
	}

	@Transactional(readOnly = true)
	public List<AuditLogResponse> search(String entityType, String actor) {
		return auditLogRepository.search(entityType, actor).stream().map(this::toResponse).toList();
	}

	private AuditLogResponse toResponse(AuditLog a) {
		return new AuditLogResponse(a.getId(), a.getAction(), a.getEntityType(), a.getEntityId(), a.getActorUsername(),
				a.getDetailsJson(), a.getCreatedAt());
	}
}
