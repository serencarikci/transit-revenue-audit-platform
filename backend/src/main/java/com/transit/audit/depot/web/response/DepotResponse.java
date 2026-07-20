package com.transit.audit.depot.web.response;

import java.time.Instant;

public record DepotResponse(Long id, String code, String name, boolean active, Instant createdAt, Instant updatedAt,
		Long version) {
}
