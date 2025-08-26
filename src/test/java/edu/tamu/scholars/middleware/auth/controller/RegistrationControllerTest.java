package edu.tamu.scholars.middleware.auth.controller;

import static edu.tamu.scholars.middleware.auth.RegistrationTestUtility.getMockRegistration;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.put;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.token.Token;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import edu.tamu.scholars.middleware.auth.RegistrationIntegrationTest;
import edu.tamu.scholars.middleware.auth.controller.request.Registration;
import edu.tamu.scholars.middleware.service.EmailService;
import edu.tamu.scholars.middleware.utility.ConstraintDescriptionsHelper;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
class RegistrationControllerTest extends RegistrationIntegrationTest {

    @TestConfiguration
    static class RegistrationControllerTestContextConfiguration {

        @Bean
        BCryptPasswordEncoder bCryptPasswordEncoder() {
            return new BCryptPasswordEncoder();
        }

    }

    private static final ConstraintDescriptionsHelper describeRegistration = new ConstraintDescriptionsHelper(Registration.class);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EmailService emailService;

    @BeforeEach
    void setup() {
        doNothing().when(emailService).send(any(String.class), any(String.class), any(String.class));
    }

    @Test
    void testSubmit() throws Exception {
        Registration registration = getMockRegistration("Bob", "Boring", "bboring@mailinator.com");

        String body = objectMapper.writeValueAsString(registration);

        // @formatter:off
        mockMvc.perform(post("/registration").contentType(APPLICATION_JSON).content(body))
            .andExpect(status().isOk())
            .andExpect(content().json("{'firstName':'Bob','lastName':'Boring','email':'bboring@mailinator.com'}"))
            .andDo(
                document(
                    "registration/submit",
                    requestFields(
                        describeRegistration.withField("firstName", "The first name of the registered account."),
                        describeRegistration.withField("lastName", "The last name of the registered account."),
                        // describeRegistration.withField("email", "The e-mail address of the registered account.")
                        // NOTE: Can't find resource for bundle java.util.PropertyResourceBundle, key edu.tamu.scholars.middleware.auth.annotation.AvailableEmail.description
                        fieldWithPath("email").description("The e-mail address of the registered account.")
                    )
                )
            );
        // @formatter:on
    }

    @Test
    void testSubmitFirstNameToShort() throws Exception {
        Registration registration = getMockRegistration("Bob", "Boring", "bboring@mailinator.com");
        registration.setFirstName("B");

        String body = objectMapper.writeValueAsString(registration);

        // @formatter:off
        mockMvc.perform(post("/registration").contentType(APPLICATION_JSON).content(body))
            .andExpect(status().isBadRequest())
            .andExpect(content().string(equalTo("First name must be between 2 and 64 characters")));
        // @formatter:on
    }

    @Test
    void testSubmitFirstNameToLong() throws Exception {
        Registration registration = getMockRegistration("Bob", "Boring", "bboring@mailinator.com");
        registration.setFirstName("Bobobobobobobobobobobobobobobobobobobobbobobobobobobobobbobobobobobobobob");

        String body = objectMapper.writeValueAsString(registration);

        // @formatter:off
        mockMvc.perform(post("/registration").contentType(APPLICATION_JSON).content(body))
            .andExpect(status().isBadRequest())
            .andExpect(content().string(equalTo("First name must be between 2 and 64 characters")));
        // @formatter:on
    }

    @Test
    void testSubmitLastNameToShort() throws Exception {
        Registration registration = getMockRegistration("Bob", "Boring", "bboring@mailinator.com");
        registration.setLastName("B");

        String body = objectMapper.writeValueAsString(registration);

        // @formatter:off
        mockMvc.perform(post("/registration").contentType(APPLICATION_JSON).content(body))
            .andExpect(status().isBadRequest())
            .andExpect(content().string(equalTo("Last name must be between 2 and 64 characters")));
        // @formatter:on
    }

    @Test
    void testSubmitLastNameToLong() throws Exception {
        Registration registration = getMockRegistration("Bob", "Boring", "bboring@mailinator.com");
        registration.setLastName("Boringinginginginginginginginginginginginginginginginginginginginginginginging");

        String body = objectMapper.writeValueAsString(registration);

        // @formatter:off
        mockMvc.perform(post("/registration").contentType(APPLICATION_JSON).content(body))
            .andExpect(status().isBadRequest())
            .andExpect(content().string(equalTo("Last name must be between 2 and 64 characters")));
        // @formatter:on
    }

    @Test
    void testSubmitEmailAlreadyInUse() throws Exception {
        createMockUser();

        Registration registration = getMockRegistration("Bob", "Boring", "bboring@mailinator.com");

        String body = objectMapper.writeValueAsString(registration);

        // @formatter:off
        mockMvc.perform(post("/registration").contentType(APPLICATION_JSON).content(body))
            .andExpect(status().isBadRequest())
            .andExpect(content().string(equalTo("Email already in use")));
        // @formatter:on
    }

    @Test
    void testConfirm() throws Exception {
        testSubmit();
        Token token = getMockToken("Bob", "Boring", "bboring@mailinator.com");
        // @formatter:off
        mockMvc.perform(get("/registration").param("key", token.getKey()))
            .andExpect(status().isOk())
            .andExpect(content().json("{'firstName':'Bob','lastName':'Boring','email':'bboring@mailinator.com'}"))
            .andDo(
                document(
                    "registration/confirm",
                    queryParameters(
                        parameterWithName("key").description("The registration verification key.")
                    )
                )
            );
        // @formatter:on
    }

    @Test
    void testConfirmAlreadyConfirmed() throws Exception {
        testConfirm();
        Token token = getMockToken("Bob", "Boring", "bboring@mailinator.com");
        // @formatter:off
        mockMvc.perform(get("/registration").param("key", token.getKey()))
            .andExpect(status().isBadRequest())
            .andExpect(content().string(equalTo("Unable to confirm registration. Email bboring@mailinator.com has already been confirmed")));
        // @formatter:on
    }

    @Test
    void testConfirmWithoutSubmit() throws Exception {
        Token token = getMockToken("Bob", "Boring", "bboring@mailinator.com");
        // @formatter:off
        mockMvc.perform(get("/registration").param("key", token.getKey()))
            .andExpect(status().isBadRequest())
            .andExpect(content().string(equalTo("Unable to confirm registration. Email bboring@mailinator.com not found")));
        // @formatter:on
    }

    @Test
    void testComplete() throws Exception {
        testConfirm();

        Registration registration = getMockRegistration("Bob", "Boring", "bboring@mailinator.com");
        registration.setPassword("HelloWorld123!");
        registration.setConfirm("HelloWorld123!");

        Token token = getMockToken("Bob", "Boring", "bboring@mailinator.com");

        String body = objectMapper.writeValueAsString(registration);

        // @formatter:off
        mockMvc.perform(put("/registration")
            .param("key", token.getKey())
            .contentType(APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(content().json("{'firstName':'Bob','lastName':'Boring','email':'bboring@mailinator.com','role':'ROLE_SUPER_ADMIN','active':true,'enabled':true}"))
                .andDo(
                    document(
                        "registration/complete",
                        requestFields(
                            describeRegistration.withField("firstName", "The first name of the registered account."),
                            describeRegistration.withField("lastName", "The last name of the registered account."),
                            // describeRegistration.withField("email", "The e-mail address of the registered account."),
                            // NOTE: Can't find resource for bundle java.util.PropertyResourceBundle, key edu.tamu.scholars.middleware.auth.annotation.AvailableEmail.description
                            fieldWithPath("email").description("The e-mail address of the registered account."),
                            describeRegistration.withField("password", "The password for the registered account."),
                            describeRegistration.withField("confirm", "The password confirmation, which should match the password.")
                        )
                    )
                );
        // @formatter:on
    }

    @Test
    void testCompleteWithoutSubmit() throws Exception {
        Registration registration = getMockRegistration("Bob", "Boring", "bboring@mailinator.com");
        registration.setPassword("HelloWorld123!");
        registration.setConfirm("HelloWorld123!");

        Token token = getMockToken("Bob", "Boring", "bboring@mailinator.com");

        String body = objectMapper.writeValueAsString(registration);

        // @formatter:off
        mockMvc.perform(put("/registration")
            .param("key", token.getKey())
            .contentType(APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(equalTo("Unable to complete registration. Email bboring@mailinator.com not found")));
        // @formatter:on
    }

    @Test
    void testCompleteWithoutConfirm() throws Exception {
        testSubmit();
        Registration registration = getMockRegistration("Bob", "Boring", "bboring@mailinator.com");
        registration.setPassword("HelloWorld123!");
        registration.setConfirm("HelloWorld123!");

        Token token = getMockToken("Bob", "Boring", "bboring@mailinator.com");

        String body = objectMapper.writeValueAsString(registration);

        // @formatter:off
        mockMvc.perform(put("/registration")
            .param("key", token.getKey())
            .contentType(APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(equalTo("Unable to complete registration. Email bboring@mailinator.com has not yet been confirmed")));
        // @formatter:on
    }

    @Test
    void testCompleteInvalidPasswordToShort() throws Exception {
        Registration registration = getMockRegistration("Bob", "Boring", "bboring@mailinator.com");
        registration.setPassword("aA1~");
        registration.setConfirm("aA1~");

        Token token = getMockToken("Bob", "Boring", "bboring@mailinator.com");

        String body = objectMapper.writeValueAsString(registration);

        // @formatter:off
        mockMvc.perform(put("/registration")
            .param("key", token.getKey())
            .contentType(APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(equalTo("Password must be at least 8 characters in length")));
        // @formatter:on
    }

    @Test
    void testCompleteInvalidPasswordToLong() throws Exception {
        Registration registration = getMockRegistration("Bob", "Boring", "bboring@mailinator.com");
        registration.setPassword("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ123456789~!@#$%^&*()_+");
        registration.setConfirm("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ123456789~!@#$%^&*()_+");

        Token token = getMockToken("Bob", "Boring", "bboring@mailinator.com");

        String body = objectMapper.writeValueAsString(registration);

        // @formatter:off
        mockMvc.perform(put("/registration")
            .param("key", token.getKey())
            .contentType(APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(equalTo("Password must be no more than 64 characters in length")));
        // @formatter:on
    }

    @Test
    void testCompleteInvalidPasswordWithWhitespace() throws Exception {
        Registration registration = getMockRegistration("Bob", "Boring", "bboring@mailinator.com");
        registration.setPassword("Hello, World 123!");
        registration.setConfirm("Hello, World 123!");

        Token token = getMockToken("Bob", "Boring", "bboring@mailinator.com");

        String body = objectMapper.writeValueAsString(registration);

        // @formatter:off
        mockMvc.perform(put("/registration")
            .param("key", token.getKey())
            .contentType(APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(equalTo("Password cannot contain whitespace characters")));
        // @formatter:on
    }

    @Test
    void testCompleteInvalidPasswordMissingLowercase() throws Exception {
        Registration registration = getMockRegistration("Bob", "Boring", "bboring@mailinator.com");
        registration.setPassword("HELLOWORLD123!");
        registration.setConfirm("HELLOWORLD123!");

        Token token = getMockToken("Bob", "Boring", "bboring@mailinator.com");

        String body = objectMapper.writeValueAsString(registration);

        // @formatter:off
        mockMvc.perform(put("/registration")
            .param("key", token.getKey())
            .contentType(APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(equalTo("Password must contain at least 1 lowercase characters")));
        // @formatter:on
    }

    @Test
    void testCompleteInvalidPasswordMissingUppercase() throws Exception {
        Registration registration = getMockRegistration("Bob", "Boring", "bboring@mailinator.com");
        registration.setPassword("helloworld123!");
        registration.setConfirm("helloworld123!");

        Token token = getMockToken("Bob", "Boring", "bboring@mailinator.com");

        String body = objectMapper.writeValueAsString(registration);

        // @formatter:off
        mockMvc.perform(put("/registration")
            .param("key", token.getKey())
            .contentType(APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(equalTo("Password must contain at least 1 uppercase characters")));
        // @formatter:on
    }

    @Test
    void testCompleteInvalidPasswordMissingSpecialCharacter() throws Exception {
        Registration registration = getMockRegistration("Bob", "Boring", "bboring@mailinator.com");
        registration.setPassword("HelloWorld123");
        registration.setConfirm("HelloWorld123");

        Token token = getMockToken("Bob", "Boring", "bboring@mailinator.com");

        String body = objectMapper.writeValueAsString(registration);

        // @formatter:off
        mockMvc.perform(put("/registration")
            .param("key", token.getKey())
            .contentType(APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(equalTo("Password must contain at least 1 special characters")));
        // @formatter:on
    }

    @Test
    void testCompletePasswordDoNotMatch() throws Exception {
        Registration registration = getMockRegistration("Bob", "Boring", "bboring@mailinator.com");
        registration.setPassword("HelloWorld123!");
        registration.setConfirm("HelloWorld123~");

        Token token = getMockToken("Bob", "Boring", "bboring@mailinator.com");

        String body = objectMapper.writeValueAsString(registration);

        // @formatter:off
        mockMvc.perform(put("/registration")
            .param("key", token.getKey())
            .contentType(APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(equalTo("Passwords do not match")));
        // @formatter:on
    }

}
