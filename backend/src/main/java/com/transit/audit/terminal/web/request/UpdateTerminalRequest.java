package com.transit.audit.terminal.web.request;

import com.transit.audit.terminal.domain.model.TerminalStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateTerminalRequest(@NotBlank @Size(max = 64) String serialNumber, @NotNull TerminalStatus status,
		@NotNull Long version) {
}
