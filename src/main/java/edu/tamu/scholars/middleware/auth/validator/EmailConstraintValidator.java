package edu.tamu.scholars.middleware.auth.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import edu.tamu.scholars.middleware.auth.annotation.AvailableEmail;
import edu.tamu.scholars.middleware.auth.model.User;
import edu.tamu.scholars.middleware.auth.model.repo.UserRepo;

/**
 * {@link User} email validator. Valid when email does not exist, otherwise invalid.
 */
public class EmailConstraintValidator implements ConstraintValidator<AvailableEmail, String> {

    private final UserRepo userRepo;

    public EmailConstraintValidator(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        return !userRepo.existsByEmail(email);
    }

}
