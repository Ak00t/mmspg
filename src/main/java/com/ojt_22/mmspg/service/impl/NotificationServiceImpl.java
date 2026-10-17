package com.ojt_22.mmspg.service.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ojt_22.mmspg.entity.Notification;
import com.ojt_22.mmspg.entity.StaffUser;
import com.ojt_22.mmspg.enums.StaffUserRole;
import com.ojt_22.mmspg.enums.StaffUserStatus;
import com.ojt_22.mmspg.repository.NotificationRepository;
import com.ojt_22.mmspg.repository.StaffUserRepository;
import com.ojt_22.mmspg.service.NotificationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

	private final NotificationRepository notificationRepository;
	private final StaffUserRepository staffUserRepository;

	@Override
	@Transactional
	public Notification sendNotification(UUID staffId, String title, String message, String type) {
		Notification notification = Notification.builder()
				.staffId(staffId)
				.title(title)
				.message(message)
				.type(type)
				.isRead(false)
				.build();

		Notification saved = notificationRepository.save(notification);
		log.info("Notification dispatched to staffId {}: [{}] {}", staffId, type, title);
		return saved;
	}

	@Override
	@Transactional
	public Notification sendNotificationToCurrentStaff(String title, String message, String type) {
		UUID staffId = getCurrentStaffId();
		if (staffId != null) {
			return sendNotification(staffId, title, message, type);
		}
		log.warn("No authenticated staff found in security context for notification: [{}] {}. Broadcasting to ADMINs.", type, title);
		sendNotificationToRole(StaffUserRole.ADMIN, title, message, type);
		return null;
	}

	@Override
	public UUID getCurrentStaffId() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
			try {
				return resolveStaffId(auth.getName());
			} catch (Exception e) {
				log.warn("Unable to resolve staffId for principal {}: {}", auth.getName(), e.getMessage());
			}
		}
		return null;
	}

	@Override
	@Transactional
	public void sendNotificationToAllStaff(String title, String message, String type) {
		List<StaffUser> activeStaff = staffUserRepository.findAll().stream()
				.filter(staff -> staff.getStatus() == StaffUserStatus.ACTIVE)
				.toList();

		for (StaffUser staff : activeStaff) {
			sendNotification(staff.getId(), title, message, type);
		}
	}

	@Override
	@Transactional
	public void sendNotificationToRole(StaffUserRole role, String title, String message, String type) {
		List<StaffUser> targetStaff = staffUserRepository.findAll().stream()
				.filter(staff -> staff.getStatus() == StaffUserStatus.ACTIVE && staff.getRole() == role)
				.toList();

		for (StaffUser staff : targetStaff) {
			sendNotification(staff.getId(), title, message, type);
		}
	}

	@Override
	@Transactional(readOnly = true)
	public List<Notification> getNotificationsForStaff(UUID staffId) {
		return notificationRepository.findTop20ByStaffIdOrderByCreatedAtDesc(staffId);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Notification> getRecentNotifications(UUID staffId) {
		return notificationRepository.findTop20ByStaffIdOrderByCreatedAtDesc(staffId);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Notification> getUnreadNotifications(UUID staffId) {
		return notificationRepository.findTop10ByStaffIdAndIsReadFalseOrderByCreatedAtDesc(staffId);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Notification> getNotificationsForCurrentStaff(String userIdentifier, boolean unreadOnly) {
		UUID staffId = resolveStaffId(userIdentifier);
		if (unreadOnly) {
			return getUnreadNotifications(staffId);
		}
		return getRecentNotifications(staffId);
	}

	@Override
	@Transactional(readOnly = true)
	public long countUnreadForCurrentStaff(String userIdentifier) {
		UUID staffId = resolveStaffId(userIdentifier);
		return notificationRepository.countByStaffIdAndIsReadFalse(staffId);
	}

	@Override
	@Transactional
	public Notification markAsRead(UUID notificationId) {
		Notification notification = notificationRepository.findById(notificationId)
				.orElseThrow(() -> new IllegalArgumentException("Notification not found with ID: " + notificationId));

		notification.setIsRead(true);
		return notificationRepository.save(notification);
	}

	@Override
	@Transactional
	public void markAllAsReadForStaff(UUID staffId) {
		List<Notification> unreadList = notificationRepository.findTop10ByStaffIdAndIsReadFalseOrderByCreatedAtDesc(staffId);
		for (Notification notification : unreadList) {
			notification.setIsRead(true);
		}
		notificationRepository.saveAll(unreadList);
	}

	private UUID resolveStaffId(String userIdentifier) {
		return staffUserRepository.findByEmail(userIdentifier)
				.map(StaffUser::getId)
				.orElseGet(() -> {
					try {
						UUID parsedId = UUID.fromString(userIdentifier);
						if (staffUserRepository.existsById(parsedId)) {
							return parsedId;
						}
					} catch (IllegalArgumentException ignored) {
					}
					throw new IllegalArgumentException("Staff user not found for identifier: " + userIdentifier);
				});
	}
}
