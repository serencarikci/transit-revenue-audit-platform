package com.transit.audit.depot.web;

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
import com.transit.audit.depot.application.DepotService;
import com.transit.audit.depot.web.request.CreateDepotRequest;
import com.transit.audit.depot.web.request.SetDepotActiveRequest;
import com.transit.audit.depot.web.request.UpdateDepotRequest;
import com.transit.audit.depot.web.response.DepotResponse;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/depots")
@Tag(name = "Depots")
public class DepotController {

	private final DepotService depotService;

	public DepotController(DepotService depotService) {
		this.depotService = depotService;
	}

	@GetMapping
	@PreAuthorize("isAuthenticated()")
	public PagedModel<DepotResponse> listDepots(@RequestParam(required = false) Boolean active, Pageable pageable) {
		return new PagedModel<>(depotService.listDepots(active, pageable));
	}

	@GetMapping("/{id}")
	@PreAuthorize("isAuthenticated()")
	public DepotResponse getDepot(@PathVariable Long id) {
		return depotService.getDepot(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("hasAnyRole('ADMIN','FINANCE_USER')")
	public DepotResponse createDepot(@Valid @RequestBody CreateDepotRequest request) {
		return depotService.createDepot(request);
	}

	@PatchMapping("/{id}")
	@PreAuthorize("hasAnyRole('ADMIN','FINANCE_USER')")
	public DepotResponse updateDepot(@PathVariable Long id, @Valid @RequestBody UpdateDepotRequest request,
			@RequestHeader(value = "If-Match", required = false) String ifMatch) {
		IfMatchSupport.requireMatchesBodyVersion(ifMatch, request.version());
		return depotService.updateDepot(id, request);
	}

	@PatchMapping("/{id}/active")
	@PreAuthorize("hasAnyRole('ADMIN','FINANCE_USER')")
	public DepotResponse setActive(@PathVariable Long id, @Valid @RequestBody SetDepotActiveRequest request,
			@RequestHeader(value = "If-Match", required = false) String ifMatch) {
		IfMatchSupport.requireMatchesBodyVersion(ifMatch, request.version());
		return depotService.setActive(id, request);
	}
}
