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
import com.ojt_22.mmspg.dto.AuditLogCreateRequest;
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

			HttpServletRequest request = null;
			ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
					.getRequestAttributes();
			if (attributes != null) {
				request = attributes.getRequest();
			}

			UUID actorId = null;
			String actorType = "STAFF";
			String roleName = "UNKNOWN";

			Authentication auth = SecurityContextHolder.getContext()
					.getAuthentication();
			if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
				try {
					actorId = UUID.fromString(auth.getName());
				} catch (IllegalArgumentException e) {
					actorId = null; // Safeguard if username is non-UUID format
				}

				roleName = auth.getAuthorities()
						.stream()
						.map(a -> a.getAuthority()
								.replace("ROLE_", ""))
						.findFirst()
						.orElse("UNKNOWN");
			}

			if (actorId == null) {
				actorId = UUID.fromString("00000000-0000-0000-0000-000000000000");
			}

			AuditLogCreateRequest auditRequest = AuditLogCreateRequest.builder()
					.actorId(actorId)
					.actorType(actorType)
					.roleName(roleName)
					.permissionUsed(auditable.permissionUsed()
							.isEmpty() ? null : auditable.permissionUsed())
					.menuName(auditable.menuName())
					.action(auditable.action())
					.description(auditable.description()
							.isEmpty() ? null : auditable.description())
					.targetType(auditable.targetType()
							.isEmpty() ? null : auditable.targetType())
					.targetId(null)
					.status(status)
					.sourceType(SourceType.BACKEND_API)
					.build();

			auditLogService.logActivity(auditRequest, request);
		}
	}
}