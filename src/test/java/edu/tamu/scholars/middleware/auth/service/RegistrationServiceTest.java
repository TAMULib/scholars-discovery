package edu.tamu.scholars.middleware.auth.service;

import static edu.tamu.scholars.middleware.auth.RegistrationTestUtility.getMockRegistration;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.util.Files;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.token.Token;
import org.springframework.security.core.token.TokenService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import edu.tamu.scholars.middleware.auth.RegistrationIntegrationTest;
import edu.tamu.scholars.middleware.auth.config.AuthConfig;
import edu.tamu.scholars.middleware.auth.controller.exception.RegistrationException;
import edu.tamu.scholars.middleware.auth.controller.request.Registration;
import edu.tamu.scholars.middleware.auth.model.Role;
import edu.tamu.scholars.middleware.auth.model.User;
import edu.tamu.scholars.middleware.auth.model.repo.UserRepo;
import edu.tamu.scholars.middleware.config.model.MailConfig;
import edu.tamu.scholars.middleware.config.model.MiddlewareConfig;
import edu.tamu.scholars.middleware.service.EmailService;
import edu.tamu.scholars.middleware.service.TemplateService;

@DataJpaTest
class RegistrationServiceTest extends RegistrationIntegrationTest {

    @TestConfiguration
    static class RegistrationServiceTestContextConfiguration {

        @Bean
        MiddlewareConfig middlewareConfig() {
            return new MiddlewareConfig();
        }

        @Bean
        AuthConfig authConfig() {
            return new AuthConfig();
        }

        @Bean
        RegistrationService registrationService(UserRepo userRepo, TokenService tokenService, EmailService emailService) {
            return new RegistrationService(
                this.authConfig(),
                userRepo,
                this.templateService(),
                emailService,
                tokenService,
                this.messageSource(),
                this.objectMapper(),
                this.passwordEncoder(),
                this.simpMessageTemplate()
            );
        }

        @Bean
        TemplateService templateService() {
            return new TemplateService();
        }

        @Bean
        EmailService emailService(JavaMailSender emailSender, MailConfig mailConfig) {
            return new EmailService(emailSender, mailConfig);
        }

        @Bean
        MessageSource messageSource() {
            return new ResourceBundleMessageSource();
        }

        @Bean
        BCryptPasswordEncoder passwordEncoder() {
            return new BCryptPasswordEncoder();
        }

        @Bean
        ObjectMapper objectMapper() {
            return new ObjectMapper();
        }

        @Bean
        SimpMessagingTemplate simpMessageTemplate() {
            return new SimpMessagingTemplate(new MessageChannel() {
                @Override
                public boolean send(Message<?> message, long timeout) {
                    return true;
                }
            });
        }

    }

    @Autowired
    private AuthConfig authConfig;

    @Autowired
    private RegistrationService registrationService;

    @MockitoBean
    private TemplateService templateService;

    @MockitoBean
    private EmailService emailService;

    @MockitoBean
    private MessageSource messageSource;

    @BeforeEach
    void setup() {
        doReturn(Files.contentOf(new File("src/test/resources/mock/templates/email/confirm-registration.html"), Charset.defaultCharset())).when(templateService).templateConfirmRegistrationMessage(any(Registration.class), any(String.class));
        doNothing().when(emailService).send(any(String.class), any(String.class), any(String.class));
        doReturn("VIVO Scholars Discovery Confirm Registration").when(messageSource).getMessage("RegistrationService.confirmationEmailSubject", new Object[0], LocaleContextHolder.getLocale());
        doReturn("Success").when(messageSource).getMessage("RegistrationService.submitSuccess", new Object[0], LocaleContextHolder.getLocale());
        doReturn("Unable to complete registration. Email eexciting@mailinator.com not found.").when(messageSource).getMessage("RegistrationService.unableToCompleteEmailNotFound", new Object[0], LocaleContextHolder.getLocale());
        doReturn("Token has expired.").when(messageSource).getMessage("RegistrationService.tokenExpired", new Object[0], LocaleContextHolder.getLocale());
        doReturn("Email bboring@mailinator.com has not yet been confirmed").when(messageSource).getMessage("RegistrationService.emailNotConfirmed", new Object[0], LocaleContextHolder.getLocale());
        doReturn("Email bboring@mailinator.com has already been confirmed").when(messageSource).getMessage("RegistrationService.emailAlreadyConfirmed", new Object[0], LocaleContextHolder.getLocale());
        doReturn("Unable to confirm registration. Email eexciting@mailinator.com not found.").when(messageSource).getMessage("RegistrationService.unableToConfirmEmailNotFound", new Object[0], LocaleContextHolder.getLocale());
    }

    @Test
    void testCreateFirstThreeUsers() throws RegistrationException, IOException {
        createUser("Bob", "Boring", "bboring@mailinator.com", "HelloWorld123~");
        createUser("Eddie", "Exciting", "eexciting@mailinator.com", "HelloWorld123!");
        createUser("Carl", "Calamitous", "ccalamitous@mailinator.com", "HelloWorld123@");
    }

    @Test
    void testSubmit() throws JsonProcessingException {
        Registration registration = getMockRegistration("Bob", "Boring", "bboring@mailinator.com");
        registration = registrationService.submit(registration);
        assertEquals("bboring@mailinator.com", registration.getEmail());
        assertEquals("Bob", registration.getFirstName());
        assertEquals("Boring", registration.getLastName());
    }

    @Test
    void testConfirm() throws IOException, RegistrationException {
        testSubmit();
        Token token = getMockToken("Bob", "Boring", "bboring@mailinator.com");
        Registration registration = registrationService.confirm(token.getKey());
        assertEquals("bboring@mailinator.com", registration.getEmail());
        assertEquals("Bob", registration.getFirstName());
        assertEquals("Boring", registration.getLastName());
    }

    @Test
    void testConfirmEmailNotFound() throws IOException {
        Token token = getMockToken("Bob", "Boring", "bboring@mailinator.com");
        assertThrows(RegistrationException.class, () -> {
            registrationService.confirm(token.getKey());
        });
    }

    @Test
    void testConfirmEmailAlreadyConfirmed() throws IOException {
        Token token = testToken();
        assertThrows(RegistrationException.class, () -> {
            registrationService.confirm(token.getKey());
        });
    }

    @Test
    void testConfirmTokenExpired() throws IOException {
        testSubmit();
        authConfig.setRegistrationTokenDuration(0);
        Token token = getMockToken("Bob", "Boring", "bboring@mailinator.com");
        assertThrows(RegistrationException.class, () -> {
            registrationService.confirm(token.getKey());
        });
    }

    @Test
    void testComplete() throws IOException, RegistrationException {
        Token token = testToken();
        Registration registration = getMockRegistration("Bob", "Boring", "bboring@mailinator.com");
        registration.setPassword("HelloWorld123!");
        registration.setConfirm("HelloWorld123!");
        User user = registrationService.complete(token.getKey(), registration);
        assertEquals("bboring@mailinator.com", user.getEmail());
        assertEquals("Bob", user.getFirstName());
        assertEquals("Boring", user.getLastName());
        assertTrue(bCryptPasswordEncoder.matches("HelloWorld123!", user.getPassword()));
        assertEquals(0, user.getOldPasswords().size());
        assertEquals(Role.ROLE_SUPER_ADMIN, user.getRole());
        assertTrue(user.isConfirmed());
        assertTrue(user.isActive());
        assertTrue(user.isEnabled());
    }

    @Test
    void testCompleteWithoutSubmit() throws IOException {
        Registration registration = getMockRegistration("Bob", "Boring", "bboring@mailinator.com");
        registration.setPassword("HelloWorld123!");
        registration.setConfirm("HelloWorld123!");
        Token token = getMockToken("Bob", "Boring", "bboring@mailinator.com");
        assertThrows(RegistrationException.class, () -> {
            registrationService.complete(token.getKey(), registration);
        });
    }

    @Test
    void testCompleteWithoutConfirm() throws IOException {
        testSubmit();
        Registration registration = getMockRegistration("Bob", "Boring", "bboring@mailinator.com");
        registration.setPassword("HelloWorld123!");
        registration.setConfirm("HelloWorld123!");
        Token token = getMockToken("Bob", "Boring", "bboring@mailinator.com");
        assertThrows(RegistrationException.class, () -> {
            registrationService.complete(token.getKey(), registration);
        });
    }

    @AfterEach
    void cleanup() {
        authConfig.setRegistrationTokenDuration(14);
    }

    private void createUser(String firstName, String lastName, String email, String password) throws IOException, RegistrationException {
        Registration registration = getMockRegistration(firstName, lastName, email);
        registration = registrationService.submit(registration);
        assertEquals(email, registration.getEmail());
        assertEquals(firstName, registration.getFirstName());
        assertEquals(lastName, registration.getLastName());

        Token token = getMockToken(firstName, lastName, email);
        registration = registrationService.confirm(token.getKey());
        assertEquals(email, registration.getEmail());
        assertEquals(firstName, registration.getFirstName());
        assertEquals(lastName, registration.getLastName());

        registration.setPassword(password);
        registration.setConfirm(password);

        User user = registrationService.complete(token.getKey(), registration);
        assertEquals(email, user.getEmail());
        assertEquals(firstName, user.getFirstName());
        assertEquals(lastName, user.getLastName());
        assertTrue(bCryptPasswordEncoder.matches(password, user.getPassword()));
        assertEquals(0, user.getOldPasswords().size());

        if (userRepo.count() == 1) {
            assertEquals(Role.ROLE_SUPER_ADMIN, user.getRole());
        } else if (userRepo.count() == 2) {
            assertEquals(Role.ROLE_ADMIN, user.getRole());
        } else {
            assertEquals(Role.ROLE_USER, user.getRole());
        }

        assertTrue(user.isConfirmed());
        assertTrue(user.isActive());
        assertTrue(user.isEnabled());
    }

    private Token testToken() throws IOException {
        return getMockToken("Bob", "Boring", "bboring@mailinator.com");
    }

}
