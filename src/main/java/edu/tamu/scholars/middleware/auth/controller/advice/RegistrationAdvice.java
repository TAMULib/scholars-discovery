package edu.tamu.scholars.middleware.auth.controller.advice;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import edu.tamu.scholars.middleware.auth.controller.RegistrationController;
import edu.tamu.scholars.middleware.auth.model.User;

/**
 * Controller advice for handling exceptions thrown by the {@link RegistrationController}.
 * 
 * <p>This class is annotated with {@link ControllerAdvice} to provide global exception handling
 * for the {@link RegistrationController}. It handles exceptions related to invalid method arguments
 * and general exceptions by returning appropriate error messages in the response.</p>
 * 
 * <p>{@link User} registration controller advice to handle bad requests.</p>
 */
@RestController
@ControllerAdvice(assignableTypes = { RegistrationController.class })
public class RegistrationAdvice {

    // TODO: add logging
    // TODO: handle registration exception and other exceptions thrown by the RegistrationController

    /**
     * Handles {@link MethodArgumentNotValidException} exceptions thrown due to invalid method arguments.
     * 
     * <p>When a method argument validation fails, this method is invoked to return a BAD_REQUEST (400)
     * response with the default error message of the first validation error.</p>

     * @param exception the {@link MethodArgumentNotValidException} instance containing details of the error
     * @return a {@link String} containing the default error message of the first validation error
     */
    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public @ResponseBody String handleMethodArgumentNotValidException(
        MethodArgumentNotValidException exception
    ) {
        return exception.getBindingResult()
            .getAllErrors()
            .get(0)
            .getDefaultMessage();
    }

    /**
     * Handles general {@link Exception} instances that are not specifically handled by other exception handlers.
     * 
     * <p>This method returns a BAD_REQUEST (400) response with the exception's message. It is a fallback handler
     * for exceptions not covered by more specific handlers.</p>

     * @param exception the {@link Exception} instance representing the general exception
     * @return a {@link String} containing the exception's message
     */
    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler(Exception.class)
    public @ResponseBody String handleException(Exception exception) {
        return exception.getMessage();
    }

}
