package com.transit.audit.identity.web.request;

import com.transit.audit.common.domain.Role;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
		@NotBlank @Size(min = 3, max = 64) @Pattern(regexp = "^[a-zA-Z0-9._-]+$") String username,
		@NotBlank @Email @Size(max = 255) String email, @NotBlank @Size(min = 8, max = 128) String password,
		@NotNull Role role) {
}
