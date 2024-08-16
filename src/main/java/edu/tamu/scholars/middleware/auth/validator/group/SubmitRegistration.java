package edu.tamu.scholars.middleware.auth.validator.group;

import edu.tamu.scholars.middleware.auth.controller.RegistrationController;
import edu.tamu.scholars.middleware.auth.service.RegistrationService;

/**
 * Marker interface for validation groups related to user {@link Registration} submission.
 * 
 * <p>This interface is used to categorize validation rules specific to the 
 * submission phase of user registration. It is intended to be used by 
 * the {@link RegistrationController} to ensure that the appropriate 
 * validation checks are performed before invoking the `submit` method 
 * of the {@link RegistrationService}.</p>
 * 
 * <p>Implementing this interface does not provide any direct functionality by itself; 
 * it serves as a tag to identify validation scenarios where the registration 
 * process is in the submission phase.</p>
 */
public interface SubmitRegistration {

}
