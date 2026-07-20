package com.transit.audit.anomaly.web.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ResolveAnomalyRequest(@NotBlank @Size(max = 1024) String resolutionNote, @NotNull Long version) {
}
