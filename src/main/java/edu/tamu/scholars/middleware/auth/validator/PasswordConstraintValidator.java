package edu.tamu.scholars.middleware.auth.validator;

import static edu.tamu.scholars.middleware.auth.AuthConstants.PASSWORD_MAX_LENGTH;
import static edu.tamu.scholars.middleware.auth.AuthConstants.PASSWORD_MIN_LENGTH;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.passay.CharacterRule;
import org.passay.EnglishCharacterData;
import org.passay.HistoryRule;
import org.passay.LengthRule;
import org.passay.PasswordData;
import org.passay.PasswordData.HistoricalReference;
import org.passay.PasswordData.Reference;
import org.passay.PasswordValidator;
import org.passay.RuleResult;
import org.passay.WhitespaceRule;
import org.passay.spring.SpringMessageResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import edu.tamu.scholars.middleware.auth.annotation.ValidPassword;
import edu.tamu.scholars.middleware.auth.controller.request.Registration;
import edu.tamu.scholars.middleware.auth.model.User;
import edu.tamu.scholars.middleware.auth.model.repo.UserRepo;

/**
 * Validator for checking the validity of passwords for {@link User} registration.
 * 
 * <p>This class implements {@link ConstraintValidator} for the custom annotation
 * {@link ValidPassword}. It ensures that the password adheres to specified
 * security rules, such as length, character requirements, and history checks.</p>
 * 
 * <p>The validation process involves:
 * <ul>
 *   <li>Ensuring the password meets minimum and maximum length requirements.</li>
 *   <li>Requiring at least one upper-case letter, lower-case letter, digit, and special character.</li>
 *   <li>Verifying that the password has not been used previously, if applicable.</li>
 *   <li>Checking that the password does not contain whitespace.</li>
 * </ul>
 * If the password does not meet these criteria or does not match the confirmation password,
 * appropriate constraint violation messages are provided.</p>

 * @see ValidPassword
 * @see Registration
 * @see User
 * @see UserRepo
 */
public class PasswordConstraintValidator implements ConstraintValidator<ValidPassword, Registration> {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private MessageSource messageSource;

    /**
     * Validates the password of a {@link Registration} object.
     * 
     * <p>This method performs several checks on the provided password:
     * <ul>
     *   <li>Validates the password length, character composition, and history.</li>
     *   <li>Ensures the password matches the confirmation password.</li>
     * </ul>
     * If the password is invalid, it adds constraint violation messages to the context.</p>
     * 
     * @param registration The {@link Registration} object containing the password and confirmation password.
     * @param context The context in which the constraint is evaluated.
     * @return {@code true} if the password is valid and matches the confirmation password; otherwise, {@code false}.
     */
    @Override
    public boolean isValid(Registration registration, ConstraintValidatorContext context) {
        String email = registration.getEmail();
        String password = registration.getPassword();

        PasswordValidator validator = new PasswordValidator(
                // use spring message resolver
                new SpringMessageResolver(messageSource),

                // Password length rule
                new LengthRule(PASSWORD_MIN_LENGTH, PASSWORD_MAX_LENGTH),

                // Character rules
                new CharacterRule(EnglishCharacterData.UpperCase, 1),
                new CharacterRule(EnglishCharacterData.LowerCase, 1),
                new CharacterRule(EnglishCharacterData.Digit, 1),
                new CharacterRule(EnglishCharacterData.Special, 1),

                // Historical password rules
                new HistoryRule(),

                // Whitespace rule
                new WhitespaceRule());

        PasswordData passwordData = new PasswordData(password);

        Optional<User> user = userRepo.findByEmail(email);

        List<Reference> passwordReferences;

        if (user.isPresent()) {
            passwordReferences = user.get()
                .getOldPasswords()
                .stream()
                .map(pw -> new HistoricalReference(pw))
                .collect(Collectors.toList());
        } else {
            passwordReferences = new ArrayList<Reference>();
        }

        passwordData.setPasswordReferences(passwordReferences);

        if (password.equals(registration.getConfirm())) {
            RuleResult result = validator.validate(passwordData);
            if (result.isValid()) {
                return true;
            }
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(String.join(", ", validator.getMessages(result)))
                .addConstraintViolation();

            return false;
        }
        context.disableDefaultConstraintViolation();

        context.buildConstraintViolationWithTemplate(messageSource.getMessage(
            "Registration.passwordsDoNotMatch",
            new Object[0],
            LocaleContextHolder.getLocale()
        )).addConstraintViolation();

        return false;
    }

}
