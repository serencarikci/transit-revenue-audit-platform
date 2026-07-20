package com.transit.audit.identity.web.response;

import com.transit.audit.common.domain.Role;

public record LoginResponse(String accessToken, String tokenType, long expiresInSeconds, String username, Role role) {
}
