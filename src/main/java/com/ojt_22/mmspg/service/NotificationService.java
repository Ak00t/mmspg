package com.ojt_22.mmspg.service;

import java.util.List;
import java.util.UUID;

import com.ojt_22.mmspg.entity.Notification;
import com.ojt_22.mmspg.enums.StaffUserRole;

public interface NotificationService {

	/**
	 * Send a notification to a specific staff/user.
	 */
	Notification sendNotification(UUID staffId, String title, String message, String type);

	/**
	 * Send a notification to the currently authenticated staff/admin (from JWT SecurityContext).
	 */
	Notification sendNotificationToCurrentStaff(String title, String message, String type);

	/**
	 * Resolves the UUID of the currently authenticated staff/admin.
	 */
	UUID getCurrentStaffId();

	/**
	 * Broadcast a notification to all active staff/users.
	 */
	void sendNotificationToAllStaff(String title, String message, String type);

	/**
	 * Send a notification to all active staff having a specific role.
	 */
	void sendNotificationToRole(StaffUserRole role, String title, String message, String type);

	/**
	 * Retrieves recent notifications (top 20) for the specified staff ID.
	 */
	List<Notification> getNotificationsForStaff(UUID staffId);

	/**
	 * Retrieves recent notifications (top 20) for the specified staff ID.
	 */
	List<Notification> getRecentNotifications(UUID staffId);

	/**
	 * Retrieves unread notifications (top 10) for the specified staff ID.
	 */
	List<Notification> getUnreadNotifications(UUID staffId);

	/**
	 * Retrieves notifications for the staff identified by email or UUID.
	 */
	List<Notification> getNotificationsForCurrentStaff(String userIdentifier, boolean unreadOnly);

	/**
	 * Counts unread notifications for the staff identified by email or UUID.
	 */
	long countUnreadForCurrentStaff(String userIdentifier);

	/**
	 * Updates the isRead status to true for the given notification ID.
	 */
	Notification markAsRead(UUID notificationId);

	/**
	 * Marks all unread notifications as read for the specified staff ID.
	 */
	void markAllAsReadForStaff(UUID staffId);
}
