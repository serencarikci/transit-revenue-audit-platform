package com.transit.audit.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.reporting")
public record ReportingProperties(String outputDir) {
}
