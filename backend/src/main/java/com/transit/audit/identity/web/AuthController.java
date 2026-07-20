package com.transit.audit.identity.web;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.transit.audit.identity.application.AuthService;
import com.transit.audit.identity.web.request.LoginRequest;
import com.transit.audit.identity.web.response.LoginResponse;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Auth")
public class AuthController {

	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping("/login")
	@PreAuthorize("permitAll()")
	public LoginResponse login(@Valid @RequestBody LoginRequest request) {
		return authService.login(request.username(), request.password());
	}
}
