package com.transit.audit.depot.web.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateDepotRequest(@NotBlank @Size(max = 128) String name, @NotNull Long version) {
}
