package com.transit.audit.terminal.web.request;

import jakarta.validation.constraints.NotNull;

public record SetTerminalActiveRequest(@NotNull Boolean active, @NotNull Long version) {
}
