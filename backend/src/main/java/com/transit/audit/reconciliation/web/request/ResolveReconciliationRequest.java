package com.transit.audit.reconciliation.web.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ResolveReconciliationRequest(@NotBlank @Size(max = 1024) String resolutionNote, @NotNull Long version) {
}
