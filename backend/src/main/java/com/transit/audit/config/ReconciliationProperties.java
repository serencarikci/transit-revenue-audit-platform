package com.transit.audit.config;

import java.math.BigDecimal;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.reconciliation")
public record ReconciliationProperties(BigDecimal smallVarianceThreshold) {
}
