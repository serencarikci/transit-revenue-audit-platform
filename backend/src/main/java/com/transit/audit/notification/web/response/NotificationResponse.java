package com.transit.audit.notification.web.response;

import java.time.Instant;

public record NotificationResponse(String type, String message, Instant createdAt) {
}
