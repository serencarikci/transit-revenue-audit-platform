package com.transit.audit.identity.web.request;

import com.transit.audit.common.domain.Role;

import jakarta.validation.constraints.NotNull;

public record UpdateUserRoleRequest(@NotNull Role role, @NotNull Long version) {
}
