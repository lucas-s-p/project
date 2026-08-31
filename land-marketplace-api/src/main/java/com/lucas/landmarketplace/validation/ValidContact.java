package com.lucas.landmarketplace.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * Validates that a contact string is either a plausible email address or a plausible phone
 * number, since a land plot's contact can be either.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidContactValidator.class)
public @interface ValidContact {

    String message() default "Contact must be a valid email address or phone number";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
