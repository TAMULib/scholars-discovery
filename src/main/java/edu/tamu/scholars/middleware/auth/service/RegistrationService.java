package edu.tamu.scholars.middleware.auth.service;

import static edu.tamu.scholars.middleware.auth.model.repo.handler.UserEventHandler.USERS_CHANNEL;

import java.io.IOException;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
 * {@link User} registration service for processing registration requests.
 */
@Service
public class RegistrationService {

    private final AuthConfig authConfig;
    private final UserRepo userRepo;
    private final TemplateService templateService;
    private final EmailService emailService;
    private final TokenService tokenService;
    private final MessageSource messageSource;
    private final ObjectMapper objectMapper;
    private final BCryptPasswordEncoder passwordEncoder;
    private final SimpMessagingTemplate simpMessageTemplate;

    public RegistrationService(
        final AuthConfig authConfig,
        final UserRepo userRepo,
        final TemplateService templateService,
        final EmailService emailService,
        final TokenService tokenService,
        final MessageSource messageSource,
        final ObjectMapper objectMapper,
        final BCryptPasswordEncoder passwordEncoder,
        final SimpMessagingTemplate simpMessageTemplate
    ) {
        this.authConfig = authConfig;
        this.userRepo = userRepo;
        this.templateService = templateService;
        this.emailService = emailService;
        this.tokenService = tokenService;
        this.messageSource = messageSource;
        this.objectMapper = objectMapper;
        this.passwordEncoder = passwordEncoder;
        this.simpMessageTemplate = simpMessageTemplate;
    }

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

    public Registration confirm(String key)
        throws IOException, RegistrationException {
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
