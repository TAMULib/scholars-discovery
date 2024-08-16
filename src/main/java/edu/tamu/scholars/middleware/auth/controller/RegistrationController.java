package edu.tamu.scholars.middleware.auth.controller;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import edu.tamu.scholars.middleware.auth.controller.exception.RegistrationException;
import edu.tamu.scholars.middleware.auth.controller.request.Registration;
import edu.tamu.scholars.middleware.auth.model.User;
import edu.tamu.scholars.middleware.auth.service.RegistrationService;
import edu.tamu.scholars.middleware.auth.validator.group.CompleteRegistration;
import edu.tamu.scholars.middleware.auth.validator.group.SubmitRegistration;

/**
 * Controller for handling {@link User} registration processes.
 * 
 * <p>This controller manages the registration process for users through a series of HTTP requests. It handles
 * the following stages of user registration:</p>
 * 
 * <ol>
 * <li>Submission of registration request: Creates a disabled user and sends an email confirmation with a link
 * and token for verification.</li>
 * <li>Confirmation of email registration: Validates the token sent in 
 * the email and confirms the user's email address.</li>
 * <li>Completion of registration: Updates and activates the user's account after email confirmation.</li>
 * </ol>
 */
@RestController
@RequestMapping("/registration")
public class RegistrationController {

    @Lazy
    @Autowired
    private RegistrationService registrationService;

    /**
     * Submits a registration request.
     * 
     * <p>This method handles POST requests to the "/registration" endpoint. It accepts a {@link Registration} object
     * containing the user's registration details, validates the request using the {@link SubmitRegistration} group,
     * and processes the registration by creating a disabled user and sending a confirmation email.</p>
     * 
     * @param registration the registration details to be submitted
     * @return a {@link ResponseEntity} containing the submitted {@link Registration} object
     * @throws JsonProcessingException if there is an error processing the JSON input
     */
    @PostMapping
    public ResponseEntity<Registration> submit(
            @RequestBody @Validated(SubmitRegistration.class) Registration registration)
            throws JsonProcessingException {
        return ResponseEntity.ok(registrationService.submit(registration));
    }

    /**
     * Confirms the user's email registration.
     * 
     * <p>This method handles GET requests to the "/registration" endpoint with a required query parameter "key". 
     * It validates the provided key, confirms the user's email registration, and returns the registration details.</p>
     * 
     * @param key the confirmation key sent in the email
     * @return a {@link ResponseEntity} containing the confirmed {@link Registration} details
     * @throws JsonParseException if there is an error parsing the JSON input
     * @throws JsonMappingException if there is an error mapping the JSON to an object
     * @throws IOException if there is an I/O error
     * @throws RegistrationException if there is an error during the registration confirmation process
     */
    @GetMapping
    public ResponseEntity<Registration> confirm(
            @RequestParam(required = true) String key)
            throws JsonParseException, JsonMappingException, IOException, RegistrationException {
        return ResponseEntity.ok(registrationService.confirm(key));
    }

    /**
     * Completes the user registration process.
     * 
     * <p>This method handles PUT requests to the "/registration" endpoint with a required query parameter "key"
     * and a {@link Registration} object in the request body. It updates and activates the user's account based on
     * the provided key and registration details.</p>
     * 
     * @param key the confirmation key used to complete the registration
     * @param registration the registration details including the password
     * @return a {@link ResponseEntity} containing the updated {@link User} object
     * @throws RegistrationException if there is an error completing the registration process
     */
    @PutMapping
    public ResponseEntity<User> complete(
            @RequestParam(required = true) String key,
            @RequestBody @Validated(CompleteRegistration.class) Registration registration)
            throws RegistrationException {
        return ResponseEntity.ok(registrationService.complete(key, registration));
    }

}
