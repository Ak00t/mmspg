package com.ojt_22.mmspg.aspect;

import java.util.UUID;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.expression.MapAccessor;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.ojt_22.mmspg.annotation.Auditable;
import com.ojt_22.mmspg.dto.AuditLogCreateRequest;
import com.ojt_22.mmspg.entity.Merchant;
import com.ojt_22.mmspg.entity.StaffUser;
import com.ojt_22.mmspg.enums.AuditStatus;
import com.ojt_22.mmspg.enums.SourceType;
import com.ojt_22.mmspg.repository.MerchantRepository;
import com.ojt_22.mmspg.repository.StaffUserRepository;
import com.ojt_22.mmspg.service.AuditLogService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Aspect
@Component
@RequiredArgsConstructor
public class AuditLogAspect {

	private static final Logger log = LoggerFactory.getLogger(AuditLogAspect.class);
	private static final ExpressionParser SPEL_PARSER = new SpelExpressionParser();

	private final AuditLogService auditLogService;
	private final MerchantRepository merchantRepository;

	private final StaffUserRepository staffUserRepository;

	@Around("@annotation(auditable)")
	public Object logAuditActivity(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
		Object result = null;
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
			String actorType = "SYSTEM";
			String roleName = "UNKNOWN";

			Authentication auth = SecurityContextHolder.getContext()
					.getAuthentication();
			if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
				String email = auth.getName();

				roleName = auth.getAuthorities()
						.stream()
						.map(a -> a.getAuthority()
								.replace("ROLE_", ""))
						.findFirst()
						.orElse("UNKNOWN");

				if ("MERCHANT".equals(roleName)) {
					actorType = "MERCHANT";

					actorId = merchantRepository.findByEmail(email)
							.map(Merchant::getId)
							.orElse(null);
				} else {
					actorType = "STAFF";

					actorId = staffUserRepository.findByEmail(email)
							.map(StaffUser::getId)
							.orElse(null);
				}
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
					.targetId(resolveTargetId(auditable, joinPoint, result))
					.status(status)
					.sourceType(SourceType.BACKEND_API)
					.build();
			auditLogService.logActivity(auditRequest, request);
		}
	}

	/**
	 * Evaluates the SpEL expression in {@link Auditable#targetId()} against the
	 * method parameters (by name) and the return value (#result). Never throws:
	 * any failure results in a null target id so the request is not affected.
	 */
	private String resolveTargetId(Auditable auditable, ProceedingJoinPoint joinPoint, Object result) {
		String expression = auditable.targetId();
		if (expression == null || expression.isBlank()) {
			return null;
		}
		try {
			StandardEvaluationContext context = new StandardEvaluationContext();
			context.addPropertyAccessor(new MapAccessor());

			String[] names = ((MethodSignature) joinPoint.getSignature()).getParameterNames();
			Object[] args = joinPoint.getArgs();
			if (names != null) {
				for (int i = 0; i < names.length; i++) {
					context.setVariable(names[i], args[i]);
				}
			}
			context.setVariable("result", result);

			Object value = SPEL_PARSER.parseExpression(expression).getValue(context);
			return value != null ? String.valueOf(value) : null;
		} catch (Exception e) {
			log.warn("Could not resolve audit targetId '{}': {}", expression, e.getMessage());
			return null;
		}
	}
}
