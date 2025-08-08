package edu.tamu.scholars.middleware.auth.handler;

import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

/**
 * Spring Boot autoconfigured custom {@link AuthenticationFailureHandler}. Customized to
 * return 401 status and write exception message to response.
 */
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void onAuthenticationFailure(
        HttpServletRequest request,
        HttpServletResponse response,
        AuthenticationException exception
    ) throws IOException, ServletException {
        response.setStatus(SC_UNAUTHORIZED);
        logger.debug(" \n\n CustomAuthenticationSuccessHandler onAuthenticationFailure request uri: {}", request.getRequestURI());
        logger.debug(" \n\n CustomAuthenticationSuccessHandler onAuthenticationFailure remote addr: {}", request.getRemoteAddr());
        logger.error(" \n\n CustomAuthenticationSuccessHandler onAuthenticationFailure authentication failed: {}", exception.getMessage(), exception);
        response.getWriter().write(exception.getMessage());
    }

}
