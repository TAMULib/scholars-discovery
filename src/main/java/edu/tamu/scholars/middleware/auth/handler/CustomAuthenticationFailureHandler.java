package edu.tamu.scholars.middleware.auth.handler;

import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

/**
 * Custom implementation of {@link AuthenticationFailureHandler} for handling authentication failures.
 * 
 * <p>This handler is used to customize the response sent to the client when authentication fails, such as when a user
 * provides incorrect credentials. It returns a 401 Unauthorized status code and writes the exception message to the
 * response body.</p>
 */
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

    /**
     * Handles authentication failure by setting the HTTP response status to 401 (Unauthorized) and writing the
     * exception message to the response body.
     * 
     * <p>This method is invoked when authentication fails, for example, due to incorrect username or password. It
     * ensures that the client receives a proper error response indicating that authentication has failed.</p>

     * @param request the HTTP request that triggered the authentication failure
     * @param response the HTTP response to be sent to the client
     * @param exception the {@link AuthenticationException} representing the reason for the authentication failure
     * @throws IOException if an input or output error occurs while handling the response
     * @throws ServletException if a servlet error occurs while handling the request
     */
    @Override
    public void onAuthenticationFailure(
        HttpServletRequest request,
        HttpServletResponse response,
        AuthenticationException exception
    ) throws IOException, ServletException {
        response.setStatus(SC_UNAUTHORIZED);
        response.getWriter().write(exception.getMessage());
    }

}
