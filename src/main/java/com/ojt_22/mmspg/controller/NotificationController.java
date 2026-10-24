package com.ojt_22.mmspg.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ojt_22.mmspg.entity.Notification;
import com.ojt_22.mmspg.service.NotificationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/notifications")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"})
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Endpoints for managing staff notifications")
public class NotificationController {

	private final NotificationService notificationService;

	@GetMapping
	@Operation(summary = "Get notifications for logged-in staff", description = "Retrieves recent notifications (top 20) or unread notifications (top 10) for the authenticated staff member extracted from the JWT security context.")
	public ResponseEntity<?> getNotifications(
			Authentication authentication,
			@RequestParam(name = "unreadOnly", defaultValue = "false") boolean unreadOnly) {

		if (authentication == null || authentication.getName() == null) {
			return ResponseEntity.status(401).body(Map.of("error", "Unauthorized: No authenticated staff found in security context"));
		}

		String userIdentifier = authentication.getName();
		List<Notification> notifications = notificationService.getNotificationsForCurrentStaff(userIdentifier, unreadOnly);
		long unreadCount = notificationService.countUnreadForCurrentStaff(userIdentifier);

		Map<String, Object> response = new HashMap<>();
		response.put("notifications", notifications);
		response.put("unreadCount", unreadCount);

		return ResponseEntity.ok(response);
	}

	@PutMapping("/{id}/read")
	@Operation(summary = "Mark notification as read", description = "Marks a specific notification as read by its UUID.")
	public ResponseEntity<?> markAsRead(@PathVariable("id") UUID id) {
		try {
			Notification updated = notificationService.markAsRead(id);
			return ResponseEntity.ok(Map.of(
					"message", "Notification marked as read successfully",
					"notification", updated
			));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
		}
	}
}
