package com.transit.audit.terminal.web.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateTerminalRequest(
		@NotBlank @Size(max = 32) @Pattern(regexp = "^[A-Z0-9_-]+$", message = "only A-Z, 0-9, - and _ are allowed") String terminalNumber,
		@NotBlank @Size(max = 64) String serialNumber) {
}
