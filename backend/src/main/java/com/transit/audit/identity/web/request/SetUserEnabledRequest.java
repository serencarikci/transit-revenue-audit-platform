package com.transit.audit.identity.web.request;

import jakarta.validation.constraints.NotNull;

public record SetUserEnabledRequest(@NotNull Boolean enabled, @NotNull Long version) {
}
