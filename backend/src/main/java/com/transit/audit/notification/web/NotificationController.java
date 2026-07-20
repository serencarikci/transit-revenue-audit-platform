package com.transit.audit.notification.web;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.transit.audit.notification.application.NotificationService;
import com.transit.audit.notification.web.response.NotificationResponse;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/notifications")
@Tag(name = "Notifications")
public class NotificationController {

	private final NotificationService notificationService;

	public NotificationController(NotificationService notificationService) {
		this.notificationService = notificationService;
	}

	@GetMapping
	@PreAuthorize("hasAnyRole('ADMIN','OPERATIONS_USER','AUDITOR')")
	public List<NotificationResponse> list() {
		return notificationService.listRecent();
	}
}
