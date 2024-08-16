package edu.tamu.scholars.middleware.auth.handler;

import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

/**
 * Custom implementation of {@link AuthenticationEntryPoint} for handling unauthorized access attempts.
 * 
 * <p>This class is used to handle authentication errors when a user tries to access a protected resource without
 * proper authentication. It is configured to return a 401 Unauthorized status code and write the exception message
 * to the response body.</p>
 */
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    /**
     * Handles authentication errors by setting the HTTP response status to 401 (Unauthorized) and writing the exception
     * message to the response body.
     * 
     * <p>This method is called when an unauthenticated user attempts to access a resource that requires authentication.
     * It ensures that the client receives an appropriate error response indicating that authentication is required.</p>

     * @param request the HTTP request that caused the authentication exception
     * @param response the HTTP response to be sent to the client
     * @param exception the {@link AuthenticationException} representing the authentication error
     * @throws IOException if an input or output error occurs while handling the response
     * @throws ServletException if a servlet error occurs while handling the request
     */
    @Override
    public void commence(
        HttpServletRequest request,
        HttpServletResponse response,
        AuthenticationException exception
    ) throws IOException, ServletException {
        response.setStatus(SC_UNAUTHORIZED);
        response.getWriter().write(exception.getMessage());
    }

}
