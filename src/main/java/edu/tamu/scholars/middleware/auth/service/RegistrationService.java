package edu.tamu.scholars.middleware.auth.service;

import static edu.tamu.scholars.middleware.auth.model.repo.handler.UserEventHandler.USERS_CHANNEL;

import java.io.IOException;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.token.Token;
import org.springframework.security.core.token.TokenService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import edu.tamu.scholars.middleware.auth.config.AuthConfig;
import edu.tamu.scholars.middleware.auth.controller.exception.RegistrationException;
import edu.tamu.scholars.middleware.auth.controller.request.Registration;
import edu.tamu.scholars.middleware.auth.model.Role;
import edu.tamu.scholars.middleware.auth.model.User;
import edu.tamu.scholars.middleware.auth.model.repo.UserRepo;
import edu.tamu.scholars.middleware.messaging.CreateEntityMessage;
import edu.tamu.scholars.middleware.service.EmailService;
import edu.tamu.scholars.middleware.service.TemplateService;

/**
 * Service class for handling user registration processes.
 * 
 * <p>This class manages the {@link User} registration workflow, including submitting registration
 * requests, confirming email addresses, and completing user registration. It integrates
 * with various components such as {@link UserRepo} for user persistence, {@link TokenService}
 * for token management, and {@link EmailService} for sending confirmation emails.</p>
 * 
 * <p>The registration process involves:
 * <ol>
 *     <li>Submitting a registration request to create a new user and send a confirmation email.</li>
 *     <li>Confirming the user's email address using a verification token.</li>
 *     <li>Completing the registration by enabling the user and setting their password.</li>
 * </ol>
 * </p>
 */
@Service
public class RegistrationService {

    @Autowired
    private AuthConfig authConfig;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    protected BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private SimpMessagingTemplate simpMessageTemplate;

    /**
     * Submits a registration request.
     * 
     * <p>This method creates a new registration token and sends a confirmation email to the user.
     * It also saves the user with a default role based on the number of existing users.</p>

     * @param registration the registration request details
     * @return the submitted registration request
     * @throws JsonProcessingException if an error occurs while processing JSON
     */
    public Registration submit(Registration registration) throws JsonProcessingException {
        String registrationJson = objectMapper.writeValueAsString(registration);
        Token token = tokenService.allocateToken(registrationJson);
        String subject = messageSource.getMessage(
            "RegistrationService.confirmationEmailSubject",
            new Object[0],
            LocaleContextHolder.getLocale()
        );
        String message = templateService.templateConfirmRegistrationMessage(registration, token.getKey());
        createUser(registration);
        emailService.send(registration.getEmail(), subject, message);

        return registration;
    }

    /**
     * Confirms a user's email address using a verification token.
     * 
     * <p>This method verifies the token and updates the user status if the token is valid and not expired.
     * It throws exceptions if the token is expired or the email is already confirmed.</p>

     * @param key the verification token
     * @return the registration details if the confirmation is successful
     * @throws JsonParseException if an error occurs while parsing JSON
     * @throws JsonMappingException if an error occurs while mapping JSON to the registration object
     * @throws IOException if an I/O error occurs
     * @throws RegistrationException if the token is invalid, expired, or if the email is not found
     */
    public Registration confirm(String key)
        throws JsonParseException, JsonMappingException, IOException, RegistrationException {
        Token token = tokenService.verifyToken(key);
        String registrationJson = token.getExtendedInformation();
        Registration registration = objectMapper.readValue(registrationJson, Registration.class);
        Optional<User> user = userRepo.findByEmail(registration.getEmail());
        if (user.isPresent()) {
            if (!isTokenExpired(token)) {
                if (!user.get().isConfirmed()) {
                    user.get().setConfirmed(true);
                    userRepo.save(user.get());
                    return registration;
                }
                throw new RegistrationException(messageSource.getMessage(
                    "RegistrationService.emailAlreadyConfirmed",
                    new Object[] { registration.getEmail() },
                    LocaleContextHolder.getLocale()
                ));
            }
            userRepo.delete(user.get());
            throw new RegistrationException(messageSource.getMessage(
                "RegistrationService.tokenExpired",
                new Object[0],
                LocaleContextHolder.getLocale()
            ));
        }
        throw new RegistrationException(messageSource.getMessage(
            "RegistrationService.unableToConfirmEmailNotFound",
            new Object[] { registration.getEmail() },
            LocaleContextHolder.getLocale()
        ));
    }

    /**
     * Completes the registration process for a user.
     * 
     * <p>This method verifies the token, sets the user's password, and enables the user if the email is confirmed.
     * It throws exceptions if the token is expired or if the email is not confirmed.</p>

     * @param key the verification token
     * @param registration the registration details including the password
     * @return the updated user if the registration is completed successfully
     * @throws RegistrationException if the token is invalid, expired, or if the email is not confirmed
     */
    public User complete(String key, Registration registration) throws RegistrationException {
        Token token = tokenService.verifyToken(key);
        Optional<User> user = userRepo.findByEmail(registration.getEmail());
        if (user.isPresent()) {
            if (!isTokenExpired(token)) {
                if (user.get().isConfirmed()) {
                    user.get().setEnabled(true);
                    user.get().setPassword(passwordEncoder.encode(registration.getPassword()));
                    return userRepo.save(user.get());
                }
                throw new RegistrationException(messageSource.getMessage(
                    "RegistrationService.emailNotConfirmed",
                    new Object[] { registration.getEmail() },
                    LocaleContextHolder.getLocale()
                ));
            }
            userRepo.delete(user.get());
            throw new RegistrationException(messageSource.getMessage(
                "RegistrationService.tokenExpired",
                new Object[0],
                LocaleContextHolder.getLocale()
            ));
        }
        throw new RegistrationException(messageSource.getMessage(
            "RegistrationService.unableToCompleteEmailNotFound",
            new Object[] { registration.getEmail() },
            LocaleContextHolder.getLocale()
        ));
    }

    /**
     * Creates a new user based on the registration details and sets the appropriate role.
     * 
     * <p>This method assigns roles based on the number of existing users and broadcasts
     * the creation event to the messaging channel.</p>

     * @param registration the registration details
     */
    private synchronized void createUser(Registration registration) {
        User user = new User(registration.getFirstName(), registration.getLastName(), registration.getEmail());
        if (userRepo.count() == 0) {
            user.setRole(Role.ROLE_SUPER_ADMIN);
        } else if (userRepo.count() == 1) {
            user.setRole(Role.ROLE_ADMIN);
        } else {
            user.setRole(Role.ROLE_USER);
        }
        user = userRepo.save(user);
        simpMessageTemplate.convertAndSend(USERS_CHANNEL, new CreateEntityMessage<User>(user));
    }

    /**
     * Checks if the provided token has expired.

     * @param token the token to check
     * @return {@code true} if the token is expired, {@code false} otherwise
     */
    private boolean isTokenExpired(Token token) {
        Calendar currentTime = Calendar.getInstance();
        Calendar creationTime = Calendar.getInstance();
        creationTime.setTimeInMillis(token.getKeyCreationTime());

        return ChronoUnit.DAYS.between(
            creationTime.toInstant(),
            currentTime.toInstant()
        ) >= authConfig.getRegistrationTokenDuration();
    }

}
