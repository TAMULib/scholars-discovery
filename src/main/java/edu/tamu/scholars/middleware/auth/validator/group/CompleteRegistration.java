package edu.tamu.scholars.middleware.auth.validator.group;

import edu.tamu.scholars.middleware.auth.controller.RegistrationController;
import edu.tamu.scholars.middleware.auth.service.RegistrationService;

/**
 * Marker interface for validation groups in the registration process.
 * 
 * <p>This interface is used to categorize validation rules specific to the 
 * completion phase of user registration. It is intended to be used by 
 * the {@link RegistrationController} to ensure that the necessary 
 * validation checks are applied before invoking the `complete` method 
 * of the {@link RegistrationService}.</p>
 * 
 * <p>Implementing this interface does not add any functionality by itself, 
 * but rather serves as a tag for identifying validation scenarios where
 * the {@link Registration} process is being completed.</p>
 */
public interface CompleteRegistration {

}
