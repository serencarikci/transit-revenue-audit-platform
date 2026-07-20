package com.transit.audit.terminal.web;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.transit.audit.common.web.IfMatchSupport;
import com.transit.audit.terminal.application.TerminalService;
import com.transit.audit.terminal.web.request.CloseAssignmentRequest;
import com.transit.audit.terminal.web.request.CreateAssignmentRequest;
import com.transit.audit.terminal.web.request.CreateTerminalRequest;
import com.transit.audit.terminal.web.request.SetTerminalActiveRequest;
import com.transit.audit.terminal.web.request.UpdateTerminalRequest;
import com.transit.audit.terminal.web.response.AssignmentResponse;
import com.transit.audit.terminal.web.response.TerminalResponse;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/terminals")
@Tag(name = "Terminals")
public class TerminalController {

	private final TerminalService terminalService;

	public TerminalController(TerminalService terminalService) {
		this.terminalService = terminalService;
	}

	@GetMapping
	@PreAuthorize("isAuthenticated()")
	public PagedModel<TerminalResponse> listTerminals(@RequestParam(required = false) Boolean active,
			Pageable pageable) {
		return new PagedModel<>(terminalService.listTerminals(active, pageable));
	}

	@GetMapping("/{id}")
	@PreAuthorize("isAuthenticated()")
	public TerminalResponse getTerminal(@PathVariable Long id) {
		return terminalService.getTerminal(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("hasAnyRole('ADMIN','FINANCE_USER','OPERATIONS_USER')")
	public TerminalResponse createTerminal(@Valid @RequestBody CreateTerminalRequest request) {
		return terminalService.createTerminal(request);
	}

	@PatchMapping("/{id}")
	@PreAuthorize("hasAnyRole('ADMIN','FINANCE_USER','OPERATIONS_USER')")
	public TerminalResponse updateTerminal(@PathVariable Long id, @Valid @RequestBody UpdateTerminalRequest request,
			@RequestHeader(value = "If-Match", required = false) String ifMatch) {
		IfMatchSupport.requireMatchesBodyVersion(ifMatch, request.version());
		return terminalService.updateTerminal(id, request);
	}

	@PatchMapping("/{id}/active")
	@PreAuthorize("hasAnyRole('ADMIN','FINANCE_USER','OPERATIONS_USER')")
	public TerminalResponse setActive(@PathVariable Long id, @Valid @RequestBody SetTerminalActiveRequest request,
			@RequestHeader(value = "If-Match", required = false) String ifMatch) {
		IfMatchSupport.requireMatchesBodyVersion(ifMatch, request.version());
		return terminalService.setActive(id, request);
	}

	@GetMapping("/{id}/assignments")
	@PreAuthorize("isAuthenticated()")
	public List<AssignmentResponse> listAssignments(@PathVariable Long id) {
		return terminalService.listAssignments(id);
	}

	@PostMapping("/{id}/assignments")
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("hasAnyRole('ADMIN','FINANCE_USER','OPERATIONS_USER')")
	public AssignmentResponse createAssignment(@PathVariable Long id,
			@Valid @RequestBody CreateAssignmentRequest request) {
		return terminalService.createAssignment(id, request);
	}

	@PostMapping("/{id}/assignments/close")
	@PreAuthorize("hasAnyRole('ADMIN','FINANCE_USER','OPERATIONS_USER')")
	public AssignmentResponse closeOpenAssignment(@PathVariable Long id,
			@Valid @RequestBody CloseAssignmentRequest request) {
		return terminalService.closeOpenAssignment(id, request);
	}
}
