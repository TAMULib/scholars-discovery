package edu.tamu.scholars.middleware.auth.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.springframework.beans.factory.annotation.Autowired;

import edu.tamu.scholars.middleware.auth.annotation.AvailableEmail;
import edu.tamu.scholars.middleware.auth.model.User;
import edu.tamu.scholars.middleware.auth.model.repo.UserRepo;

/**
 * {@link User} email validator. Valid when email does not exist, otherwise invalid.
 */
public class EmailConstraintValidator implements ConstraintValidator<AvailableEmail, String> {

    @Autowired
    private UserRepo userRepo;

    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        return !userRepo.existsByEmail(email);
    }

}
