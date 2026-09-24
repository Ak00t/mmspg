package com.ojt_22.mmspg.aspect;

import java.util.UUID;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.ojt_22.mmspg.annotation.Auditable;
import com.ojt_22.mmspg.enums.AuditStatus;
import com.ojt_22.mmspg.enums.SourceType;
import com.ojt_22.mmspg.service.AuditLogService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Aspect
@Component
@RequiredArgsConstructor
public class AuditLogAspect {

	private final AuditLogService auditLogService;

	@Around("@annotation(auditable)")
	public Object logAuditActivity(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
		Object result;
		AuditStatus status = AuditStatus.SUCCESS;

		try {
			result = joinPoint.proceed();
			return result;
		} catch (Throwable ex) {
			status = AuditStatus.FAILURE;
			throw ex;
		} finally {
			// Retrieve current HTTP request
			HttpServletRequest request = null;
			ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
					.getRequestAttributes();
			if (attributes != null) {
				request = attributes.getRequest();
			}

			// Retrieve authenticated user info from SecurityContext
			UUID actorId = null;
			String actorType = "STAFF";
			String roleName = "UNKNOWN";

			Authentication auth = SecurityContextHolder.getContext()
					.getAuthentication();
			if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
				// Adjust extraction based on your UserDetails implementation
				actorId = UUID.fromString(auth.getName()); // Assuming principal username is User UUID String
				roleName = auth.getAuthorities()
						.stream()
						.map(a -> a.getAuthority()
								.replace("ROLE_", ""))
						.findFirst()
						.orElse("UNKNOWN");
			}

			// Fallback for null actorId during testing or system tasks
			if (actorId == null) {
				actorId = UUID.fromString("00000000-0000-0000-0000-000000000000");
			}

			// Save the audit log entry asynchronously or via service
			auditLogService.logActivity(actorId, actorType, roleName, auditable.permissionUsed()
					.isEmpty() ? null : auditable.permissionUsed(), auditable.menuName(), auditable.action(),
					auditable.description(), auditable.targetType()
							.isEmpty() ? null : auditable.targetType(),
					null, // Target ID can be dynamically extracted if passed in request parameters
					status, SourceType.BACKEND_API, request);
		}
	}
}