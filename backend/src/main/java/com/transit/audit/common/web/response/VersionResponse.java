package com.transit.audit.common.web.response;

import java.time.Instant;

public record VersionResponse(String name, String version, String group, Instant buildTime) {
}
