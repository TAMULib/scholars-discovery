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
 * Custom implementation of {@link AuthenticationSuccessHandler} for handling successful authentication.
 * 
 * <p>This handler customizes the response sent to the client upon successful authentication. Instead of the default
 * behavior, it returns the authenticated principal as a {@link User} object in JSON format.</p>
 */
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private ObjectMapper objectMapper;

    /**
     * Constructs a {@link CustomAuthenticationSuccessHandler} with the specified {@link ObjectMapper}.
     * 
     * <p>The {@link ObjectMapper} is used to convert the {@link User} object to JSON format for the response body.</p>

     * @param objectMapper the {@link ObjectMapper} used for serializing the {@link User} object to JSON
     */
    public CustomAuthenticationSuccessHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Handles successful authentication by returning the authenticated
     * principal as a {@link User} object in JSON format.
     * 
     * <p>This method sets the response content type to {@code application/json} and writes the serialized {@link User}
     * object to the response output stream. This provides the client with the details of the authenticated user.</p>
     * 
     * @param request the HTTP request that triggered the successful authentication
     * @param response the HTTP response to be sent to the client
     * @param authentication the {@link Authentication} object containing the authenticated user's details
     * @throws IOException if an input or output error occurs while handling the response
     * @throws ServletException if a servlet error occurs while handling the request
     */
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
