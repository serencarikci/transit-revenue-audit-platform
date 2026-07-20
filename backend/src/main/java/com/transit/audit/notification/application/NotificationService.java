package com.transit.audit.notification.application;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.transit.audit.anomaly.domain.model.Anomaly;
import com.transit.audit.notification.web.response.NotificationResponse;

@Service
public class NotificationService {

	private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

	private final List<NotificationResponse> history = new CopyOnWriteArrayList<>();

	public void notifyAnomaly(Anomaly anomaly) {
		if (anomaly == null) {
			return;
		}
		String message = "Anomaly " + anomaly.getRuleCode() + " on " + anomaly.getEntityType() + ":"
				+ anomaly.getEntityId() + " — " + anomaly.getTitle();
		publish("ANOMALY", message);
	}

	public void notifyHashMismatch(String message) {
		publish("HASH_MISMATCH", message);
	}

	public List<NotificationResponse> listRecent() {
		return List.copyOf(history);
	}

	private void publish(String type, String message) {
		log.info("NOTIFICATION [{}] {}", type, message);
		history.add(0, new NotificationResponse(type, message, Instant.now()));
		while (history.size() > 500) {
			history.remove(history.size() - 1);
		}
	}
}
