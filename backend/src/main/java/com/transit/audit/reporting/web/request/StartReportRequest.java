package com.transit.audit.reporting.web.request;

import java.time.Instant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record StartReportRequest(@NotBlank String reportType, @NotNull Instant from, @NotNull Instant to) {
}
