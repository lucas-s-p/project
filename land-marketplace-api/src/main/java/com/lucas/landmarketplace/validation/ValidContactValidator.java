package com.lucas.landmarketplace.validation;

import java.util.regex.Pattern;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidContactValidator implements ConstraintValidator<ValidContact, String> {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[0-9]{8,15}$");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }
        String digitsOnly = value.replaceAll("[\\s()-]", "");
        return EMAIL_PATTERN.matcher(value).matches() || PHONE_PATTERN.matcher(digitsOnly).matches();
    }
}
