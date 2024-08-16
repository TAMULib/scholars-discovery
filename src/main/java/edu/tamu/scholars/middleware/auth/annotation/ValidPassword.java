package edu.tamu.scholars.middleware.auth.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import javax.validation.Constraint;
import javax.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import edu.tamu.scholars.middleware.auth.model.User;
import edu.tamu.scholars.middleware.auth.validator.PasswordConstraintValidator;

/**
 * Annotation to declare that the {@link User} model's password is subject to validation
 * to ensure it meets certain criteria.
 * 
 * <p>This annotation uses the {@link PasswordConstraintValidator} to perform the actual validation
 * logic for the password.</p>
 * 
 * <p>The annotation can be applied at the class level ({@code TYPE}).</p>
 * 
 * <p>Example usage:</p>
 * 
 * <pre>
 * &#64;ValidPassword(message = "Password does not meet the required criteria")
 * public class User {
 *     // fields, methods
 * }
 * </pre>

 * @see User
 * @see PasswordConstraintValidator
 */
@Documented
@Target(TYPE)
@Retention(RUNTIME)
@Constraint(validatedBy = PasswordConstraintValidator.class)
public @interface ValidPassword {

    /**
     * The error message to be returned when the password validation fails.

     * @return the error message
     */
    String message();

    /**
     * Allows grouping of constraints. Default is an empty array, meaning no specific groups.

     * @return the groups
     */
    Class<?>[] groups() default {};

    /**
     * Provides additional data to the annotation, which can be used by clients of the annotation.
     * Default is an empty array.

     * @return the payload
     */
    Class<? extends Payload>[] payload() default {};

}
