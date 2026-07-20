package com.transit.audit.terminal.web.request;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;

public record CloseAssignmentRequest(@NotNull LocalDate validTo) {
}
