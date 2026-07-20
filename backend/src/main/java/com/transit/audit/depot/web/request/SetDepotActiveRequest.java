package com.transit.audit.depot.web.request;

import jakarta.validation.constraints.NotNull;

public record SetDepotActiveRequest(@NotNull Boolean active, @NotNull Long version) {
}
