package edu.tamu.scholars.middleware.auth.controller.exception;

import edu.tamu.scholars.middleware.auth.model.User;
import edu.tamu.scholars.middleware.auth.service.RegistrationService;

/**
 * Exception thrown during the user registration process.
 * 
 * <p>This exception is used by the {@link RegistrationService} to indicate errors that occur
 * during the registration of a {@link User}. It extends the standard {@link Exception} class and
 * includes a message that describes the cause of the registration failure.</p>
 * 
 * <p>The {@link RegistrationException} is intended to provide a way to signal and handle
 * registration-specific issues that need to be communicated to the caller.</p>
 */
public class RegistrationException extends Exception {

    private static final long serialVersionUID = 7712767291119883628L;

    /**
     * Constructs a new {@code RegistrationException} with the specified detail message.
     * 
     * @param message the detail message, which is saved for later retrieval by the {@link #getMessage()} method
     */
    public RegistrationException(String message) {
        super(message);
    }

}
