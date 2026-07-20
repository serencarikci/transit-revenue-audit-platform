package com.transit.audit.anomaly.application;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.transit.audit.anomaly.domain.model.Anomaly;
import com.transit.audit.anomaly.domain.model.AnomalySeverity;
import com.transit.audit.anomaly.domain.model.AnomalyStatus;
import com.transit.audit.anomaly.infrastructure.persistence.AnomalyRepository;
import com.transit.audit.anomaly.web.request.ResolveAnomalyRequest;
import com.transit.audit.anomaly.web.response.AnomalyResponse;
import com.transit.audit.common.concurrency.OptimisticLockSupport;
import com.transit.audit.common.exception.ResourceNotFoundException;

@Service
public class AnomalyService {

	private final AnomalyRepository anomalyRepository;

	public AnomalyService(AnomalyRepository anomalyRepository) {
		this.anomalyRepository = anomalyRepository;
	}

	@Transactional(readOnly = true)
	public List<AnomalyResponse> search(AnomalySeverity severity, AnomalyStatus status, String entityType) {
		return anomalyRepository.search(severity, status, entityType).stream().map(this::toResponse).toList();
	}

	@Transactional
	public AnomalyResponse review(Long id, String reviewer) {
		Anomaly anomaly = require(id);
		anomaly.markUnderReview(reviewer);
		return toResponse(anomalyRepository.save(anomaly));
	}

	@Transactional
	public AnomalyResponse resolve(Long id, ResolveAnomalyRequest request, String reviewer) {
		Anomaly anomaly = require(id);
		OptimisticLockSupport.requireVersion(anomaly.getVersion(), request.version(), "Anomaly");
		anomaly.resolve(reviewer, request.resolutionNote());
		return toResponse(anomalyRepository.save(anomaly));
	}

	private Anomaly require(Long id) {
		return anomalyRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Anomaly", id));
	}

	private AnomalyResponse toResponse(Anomaly a) {
		return new AnomalyResponse(a.getId(), a.getRuleCode(), a.getSeverity(), a.getStatus(), a.getEntityType(),
				a.getEntityId(), a.getTitle(), a.getDetails(), a.getDetectedAt(), a.getReviewedBy(),
				a.getResolutionNote(), a.getResolvedAt(), a.getVersion());
	}
}
