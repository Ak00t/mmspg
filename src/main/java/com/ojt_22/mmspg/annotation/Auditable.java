package com.ojt_22.mmspg.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Auditable {
	String menuName();

	String action();

	String description() default "";

	String targetType() default "";

	/**
	 * Optional SpEL expression to resolve the audit target_id. Method parameters
	 * are available by name (e.g. "#id") and the return value as "#result" (e.g.
	 * "#result.body.branchId"). Empty means no target id.
	 */
	String targetId() default "";

	String permissionUsed() default "";
}