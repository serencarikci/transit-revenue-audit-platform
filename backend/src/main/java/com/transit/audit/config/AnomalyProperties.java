package com.transit.audit.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.anomaly")
public record AnomalyProperties(int futureDateToleranceMinutes, int cancelSaleProximitySeconds,
		int frequentTransferMaxCount, int frequentTransferWindowHours, int maxOpenShiftHours, String scanCron) {
}
