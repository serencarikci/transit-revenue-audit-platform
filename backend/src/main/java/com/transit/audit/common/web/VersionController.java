package com.transit.audit.common.web;

import java.util.Optional;

import org.springframework.boot.info.BuildProperties;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.transit.audit.common.web.response.VersionResponse;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/version")
@Tag(name = "Version")
public class VersionController {

	private final Optional<BuildProperties> buildProperties;

	public VersionController(Optional<BuildProperties> buildProperties) {
		this.buildProperties = buildProperties;
	}

	@GetMapping
	@PreAuthorize("permitAll()")
	public VersionResponse getVersion() {
		return buildProperties
				.map(build -> new VersionResponse(build.getName(), build.getVersion(), build.getGroup(), build.getTime()))
				.orElseGet(() -> new VersionResponse("transit-revenue-audit-platform", "unknown", "com.transit", null));
	}
}
