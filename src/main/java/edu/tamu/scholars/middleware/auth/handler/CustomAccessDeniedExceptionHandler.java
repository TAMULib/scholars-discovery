package edu.tamu.scholars.middleware.auth.handler;

import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

/**
 * Custom implementation of {@link AccessDeniedHandler} for handling access denied exceptions.
 * 
 * <p>This handler is used to customize the response when a user tries to access a resource they are not authorized to
 * access. It is configured to return a 401 Unauthorized status code and to write the exception message to the response
 * body.</p>
 */
public class CustomAccessDeniedExceptionHandler implements AccessDeniedHandler {

    /**
     * Handles access denied exceptions.
     * 
     * <p>This method is called when a user attempts to access a resource for which they do not have the necessary
     * permissions. It sets the HTTP response status to 401 (Unauthorized) and writes the exception message to the
     * response body.</p>
     * 
     * @param request the HTTP request that caused the exception
     * @param response the HTTP response to be sent
     * @param exception the exception representing the access denied error
     * @throws IOException if an input or output error occurs while handling the response
     * @throws ServletException if a servlet error occurs while handling the request
     */
    @Override
    public void handle(
        HttpServletRequest request,
        HttpServletResponse response,
        AccessDeniedException exception
    ) throws IOException, ServletException {
        response.setStatus(SC_UNAUTHORIZED);
        response.getWriter().write(exception.getMessage());
    }

}
