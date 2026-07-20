package com.transit.audit.identity.web;

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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.transit.audit.common.web.IfMatchSupport;
import com.transit.audit.identity.application.UserService;
import com.transit.audit.identity.web.request.CreateUserRequest;
import com.transit.audit.identity.web.request.SetUserEnabledRequest;
import com.transit.audit.identity.web.request.UpdateUserRoleRequest;
import com.transit.audit.identity.web.response.UserResponse;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Users")
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

	private final UserService userService;

	public UserController(UserService userService) {
		this.userService = userService;
	}

	@GetMapping
	public PagedModel<UserResponse> listUsers(Pageable pageable) {
		return new PagedModel<>(userService.listUsers(pageable));
	}

	@GetMapping("/{id}")
	public UserResponse getUser(@PathVariable Long id) {
		return userService.getUser(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public UserResponse createUser(@Valid @RequestBody CreateUserRequest request) {
		return userService.createUser(request);
	}

	@PatchMapping("/{id}/role")
	public UserResponse changeRole(@PathVariable Long id, @Valid @RequestBody UpdateUserRoleRequest request,
			@RequestHeader(value = "If-Match", required = false) String ifMatch) {
		IfMatchSupport.requireMatchesBodyVersion(ifMatch, request.version());
		return userService.changeRole(id, request);
	}

	@PatchMapping("/{id}/enabled")
	public UserResponse setEnabled(@PathVariable Long id, @Valid @RequestBody SetUserEnabledRequest request,
			@RequestHeader(value = "If-Match", required = false) String ifMatch) {
		IfMatchSupport.requireMatchesBodyVersion(ifMatch, request.version());
		return userService.setEnabled(id, request);
	}
}
