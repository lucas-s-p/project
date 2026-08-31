package com.lucas.landmarketplace.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintValidatorContext;

class ValidContactValidatorTest {

    private final ValidContactValidator validator = new ValidContactValidator();
    private final ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);

    @Test
    void isValid_acceptsEmailAddress() {
        assertThat(validator.isValid("owner@example.com", context)).isTrue();
    }

    @Test
    void isValid_acceptsPlainPhoneNumber() {
        assertThat(validator.isValid("+5511987654321", context)).isTrue();
    }

    @Test
    void isValid_acceptsFormattedPhoneNumber() {
        assertThat(validator.isValid("(11) 98765-4321", context)).isTrue();
    }

    @Test
    void isValid_rejectsTextThatIsNeitherEmailNorPhone() {
        assertThat(validator.isValid("not-a-contact", context)).isFalse();
    }

    @Test
    void isValid_rejectsTooShortPhoneNumber() {
        assertThat(validator.isValid("1234", context)).isFalse();
    }

    @Test
    void isValid_acceptsBlank_andLetsNotBlankHandleIt() {
        assertThat(validator.isValid("", context)).isTrue();
        assertThat(validator.isValid(null, context)).isTrue();
    }
}
