package com.transit.audit.identity.web.response;

import java.time.Instant;

import com.transit.audit.common.domain.Role;

public record UserResponse(Long id, String username, String email, Role role, boolean enabled, Instant createdAt,
		Long version) {
}
