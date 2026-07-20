package com.transit.audit.auditlog.web;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.transit.audit.auditlog.application.AuditLogService;
import com.transit.audit.auditlog.web.response.AuditLogResponse;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/audit-logs")
@Tag(name = "Audit Logs")
public class AuditLogController {

	private final AuditLogService auditLogService;

	public AuditLogController(AuditLogService auditLogService) {
		this.auditLogService = auditLogService;
	}

	@GetMapping
	@PreAuthorize("hasAnyRole('ADMIN','AUDITOR')")
	public PagedModel<AuditLogResponse> list(Pageable pageable) {
		return new PagedModel<>(auditLogService.list(pageable));
	}

	@GetMapping("/search")
	@PreAuthorize("hasAnyRole('ADMIN','AUDITOR')")
	public List<AuditLogResponse> search(@RequestParam(required = false) String entityType,
			@RequestParam(required = false) String actor) {
		return auditLogService.search(entityType, actor);
	}
}
