package edu.tamu.scholars.middleware.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import jakarta.mail.Address;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import edu.tamu.scholars.middleware.config.model.MailConfig;

@ExtendWith(SpringExtension.class)
class EmailServiceTest {

    @TestConfiguration
    static class EmailServiceTestContextConfiguration {

        @Bean
        MailConfig mailConfig() {
            return new MailConfig();
        }

        @Bean
        EmailService emailService() {
            return new EmailService(emailSender(), mailConfig());
        }

        @Bean
        JavaMailSender emailSender() {
            return new JavaMailSenderImpl();
        }

    }

    @Autowired
    private EmailService emailService;

    @MockitoBean
    private JavaMailSender emailSender;

    @MockitoBean
    private MailConfig mailConfig;

    @Test
    void testSend() {
        when(mailConfig.getFrom()).thenReturn("scholarsdiscovery@gmail.com");
        when(mailConfig.getReplyTo()).thenReturn("scholarsdiscovery@gmail.com");
        doNothing().when(emailSender).send(any(MimeMessagePreparator.class));
        emailService.send("bboring@mailinator.com", "Test", "This is only a test!");
        assertTrue(true);
    }

    @Test
    void testCreateMimeMessagePreparator() throws Exception {
        MimeMessagePreparator mimeMessagePreparator = emailService.createMimeMessagePreparator("bboring@mailinator.com", "Test", "This is only a test!", "scholarsdiscovery@gmail.com", "scholarsdiscovery@gmail.com");
        MimeMessage mimeMessage = new JavaMailSenderImpl().createMimeMessage();
        mimeMessagePreparator.prepare(mimeMessage);
        Address[] to = mimeMessage.getAllRecipients();
        assertEquals(1, to.length);
        assertEquals("bboring@mailinator.com", to[0].toString());
        Address[] from = mimeMessage.getFrom();
        assertEquals(1, from.length);
        assertEquals("scholarsdiscovery@gmail.com", from[0].toString());
        Address[] replyTo = mimeMessage.getReplyTo();
        assertEquals(1, replyTo.length);
        assertEquals("scholarsdiscovery@gmail.com", replyTo[0].toString());
        assertEquals("Test", mimeMessage.getSubject());
    }

}
