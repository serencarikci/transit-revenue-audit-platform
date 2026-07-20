package com.transit.audit.anomaly.web;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.transit.audit.anomaly.application.AnomalyService;
import com.transit.audit.anomaly.domain.model.AnomalySeverity;
import com.transit.audit.anomaly.domain.model.AnomalyStatus;
import com.transit.audit.anomaly.web.request.ResolveAnomalyRequest;
import com.transit.audit.anomaly.web.response.AnomalyResponse;
import com.transit.audit.common.web.IfMatchSupport;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/anomalies")
@Tag(name = "Anomalies")
public class AnomalyController {

	private final AnomalyService anomalyService;

	public AnomalyController(AnomalyService anomalyService) {
		this.anomalyService = anomalyService;
	}

	@GetMapping
	@PreAuthorize("isAuthenticated()")
	public List<AnomalyResponse> search(@RequestParam(required = false) AnomalySeverity severity,
			@RequestParam(required = false) AnomalyStatus status, @RequestParam(required = false) String entityType) {
		return anomalyService.search(severity, status, entityType);
	}

	@PostMapping("/{id}/review")
	@PreAuthorize("hasAnyRole('ADMIN','FINANCE_USER','AUDITOR','OPERATIONS_USER')")
	public AnomalyResponse review(@PathVariable Long id, Authentication authentication) {
		return anomalyService.review(id, authentication.getName());
	}

	@PostMapping("/{id}/resolve")
	@PreAuthorize("hasAnyRole('ADMIN','FINANCE_USER','AUDITOR')")
	public AnomalyResponse resolve(@PathVariable Long id, @Valid @RequestBody ResolveAnomalyRequest request,
			@RequestHeader(value = "If-Match", required = false) String ifMatch, Authentication authentication) {
		IfMatchSupport.requireMatchesBodyVersion(ifMatch, request.version());
		return anomalyService.resolve(id, request, authentication.getName());
	}
}
