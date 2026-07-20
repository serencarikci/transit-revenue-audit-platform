package com.transit.audit.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.pda")
public record PdaProperties(int syncDelayHours, int plannedShutdownHour, String scanCron) {
}
