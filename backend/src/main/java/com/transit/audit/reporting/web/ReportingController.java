package com.transit.audit.reporting.web;

import java.nio.file.Files;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.transit.audit.common.exception.BusinessException;
import com.transit.audit.reporting.application.ReportingService;
import com.transit.audit.reporting.web.request.StartReportRequest;
import com.transit.audit.reporting.web.response.ReportSnapshotResponse;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/reports")
@Tag(name = "Reports")
public class ReportingController {

	private final ReportingService reportingService;

	public ReportingController(ReportingService reportingService) {
		this.reportingService = reportingService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.ACCEPTED)
	@PreAuthorize("hasAnyRole('ADMIN','FINANCE_USER','AUDITOR')")
	public ReportSnapshotResponse start(@Valid @RequestBody StartReportRequest request, Authentication authentication) {
		return reportingService.start(request, authentication.getName());
	}

	@GetMapping("/{id}")
	@PreAuthorize("isAuthenticated()")
	public ReportSnapshotResponse status(@PathVariable Long id) {
		return reportingService.get(id);
	}

	@GetMapping("/{id}/download")
	@PreAuthorize("hasAnyRole('ADMIN','FINANCE_USER','AUDITOR')")
	public ResponseEntity<Resource> download(@PathVariable Long id) {
		try {
			var path = reportingService.downloadPath(id);
			Resource resource = new FileSystemResource(path);
			return ResponseEntity.ok()
					.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + path.getFileName() + "\"")
					.contentType(MediaType.parseMediaType("text/csv")).contentLength(Files.size(path)).body(resource);
		} catch (BusinessException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new BusinessException("Internal Server Error", "Failed to download report: " + ex.getMessage(), 500);
		}
	}
}
