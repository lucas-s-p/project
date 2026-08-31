package com.lucas.landmarketplace.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * Validates, at class level, that a land plot's boundary polygon does not spatially overlap
 * any land plot already registered. Backed by a PostGIS {@code ST_Intersects} query.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NoOverlapValidator.class)
public @interface NoOverlap {

    String message() default "The land plot boundary overlaps an existing registered land plot";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
