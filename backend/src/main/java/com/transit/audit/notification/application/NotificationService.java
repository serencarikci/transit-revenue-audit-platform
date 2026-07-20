package com.transit.audit.notification.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.transit.audit.anomaly.domain.model.Anomaly;

@Service
public class NotificationService {

	private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

	public void notifyAnomaly(Anomaly anomaly) {
		if (anomaly == null) {
			return;
		}
		log.info("NOTIFICATION [ANOMALY] Anomaly {} on {}:{} — {}", anomaly.getRuleCode(), anomaly.getEntityType(),
				anomaly.getEntityId(), anomaly.getTitle());
	}

	public void notifyHashMismatch(String message) {
		log.info("NOTIFICATION [HASH_MISMATCH] {}", message);
	}
}
