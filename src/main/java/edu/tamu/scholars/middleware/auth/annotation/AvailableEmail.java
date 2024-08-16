package edu.tamu.scholars.middleware.auth.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import javax.validation.Constraint;
import javax.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import edu.tamu.scholars.middleware.auth.model.User;
import edu.tamu.scholars.middleware.auth.validator.EmailConstraintValidator;

/**
 * Annotation to declare that the email field of the {@link User} model is subject to validation
 * to ensure it is available (not already in use).
 * <p>This annotation uses the {@link EmailConstraintValidator} to perform the actual validation
 * logic for checking email availability.</p>
 * 
 * <p>The annotation can be applied to {@code FIELD} level.</p>
 * 
 * <p>Example usage:</p>
 * <pre>
 * &#64;AvailableEmail(message = "Email is already in use")
 * private String email;
 * </pre>

 * @see User
 * @see EmailConstraintValidator
 */
@Documented
@Target(FIELD)
@Retention(RUNTIME)
@Constraint(validatedBy = EmailConstraintValidator.class)
public @interface AvailableEmail {

    /**
     * The error message to be returned when the email validation fails.
     * 
     * @return the error message
     */
    String message();

    /**
     * Allows grouping of constraints. Default is an empty array, meaning no specific groups.
     * 
     * @return the groups
     */
    Class<?>[] groups() default {};

    /**
     * Provides additional data to the annotation, which can be used by clients of the annotation.
     * Default is an empty array.
     * 
     * @return the payload
     */
    Class<? extends Payload>[] payload() default {};

}
