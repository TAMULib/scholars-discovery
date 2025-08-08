package edu.tamu.scholars.middleware.auth.handler;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import edu.tamu.scholars.middleware.auth.model.User;

/**
 * Spring Boot autoconfigured custom {@link AuthenticationSuccessHandler}. Customized to
 * return authenticated principal as {@link User}.
 */
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private ObjectMapper objectMapper;

    public CustomAuthenticationSuccessHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void onAuthenticationSuccess(
        HttpServletRequest request,
        HttpServletResponse response,
        Authentication authentication
    ) throws IOException, ServletException {
        response.setContentType(APPLICATION_JSON_VALUE);
        response.getOutputStream()
            .write(objectMapper.writeValueAsBytes((User) authentication.getPrincipal()));
    }

}
