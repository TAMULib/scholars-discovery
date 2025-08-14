package edu.tamu.scholars.middleware.auth.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import edu.tamu.scholars.middleware.auth.model.User;
import edu.tamu.scholars.middleware.auth.validator.EmailConstraintValidator;

/**
 * Annotation to declare validated {@link User} model email is available.
 */
@Documented
@Target(FIELD)
@Retention(RUNTIME)
@Constraint(validatedBy = EmailConstraintValidator.class)
public @interface AvailableEmail {

    String message();

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
