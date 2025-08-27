package edu.tamu.scholars.middleware.auth.handler;

import static jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

/**
 * Spring Boot autoconfigured custom {@link AccessDeniedHandler}. Customized to
 * return 401 status and write exception message to response.
 */
public class CustomAccessDeniedExceptionHandler implements AccessDeniedHandler {

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
