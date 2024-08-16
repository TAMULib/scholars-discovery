package edu.tamu.scholars.middleware.auth.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.beans.factory.annotation.Autowired;

import edu.tamu.scholars.middleware.auth.annotation.AvailableEmail;
import edu.tamu.scholars.middleware.auth.model.User;
import edu.tamu.scholars.middleware.auth.model.repo.UserRepo;

/**
 * Validator for checking the availability of an {@link User} email address.
 * 
 * <p>This class implements the {@link ConstraintValidator} interface to provide
 * custom validation for email addresses. It ensures that the email being validated
 * does not already exist in the system. The validator is used in conjunction with
 * the {@link AvailableEmail} annotation to enforce uniqueness constraints on email
 * addresses.</p>
 * 
 * <p>The validation logic checks the {@link UserRepo} to determine whether the email
 * already exists. If the email is not found in the repository, it is considered valid;
 * otherwise, it is deemed invalid.</p>

 * @see AvailableEmail
 * @see UserRepo
 * @see User
 */
public class EmailConstraintValidator implements ConstraintValidator<AvailableEmail, String> {

    @Autowired
    private UserRepo userRepo;

    /**
     * Validates the email address to ensure it does not already exist.
     * 
     * @param email The email address to be validated.
     * @param context The context in which the constraint is evaluated.
     * @return {@code true} if the email does not exist in the repository, otherwise {@code false}.
     */
    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        return !userRepo.existsByEmail(email);
    }

}
