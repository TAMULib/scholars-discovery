package edu.tamu.scholars.middleware.auth.handler;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticatedPrincipal;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import edu.tamu.scholars.middleware.auth.model.User;

/**
 * Spring Boot autoconfigured custom {@link AuthenticationSuccessHandler}. Customized to
 * return authenticated principal as {@link User}.
 */
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
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

        Saml2AuthenticatedPrincipal samlPrincipal = (Saml2AuthenticatedPrincipal) authentication.getPrincipal();

        samlPrincipal.getAttributes().forEach((k, v) ->
        logger.info(" \n\n CustomAuthenticationSuccessHandler onAuthenticationSuccess SAML Attributes: {} = {}", k, v)
        );
        String email = samlPrincipal.getFirstAttribute("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/emailaddress");
        String firstName = samlPrincipal.getFirstAttribute("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/givenname");
        String lastName = samlPrincipal.getFirstAttribute("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/surname");

        User user = new User();
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        logger.info(" \n\n CustomAuthenticationSuccessHandler onAuthenticationSuccess User email: {} ", user.getEmail());
        logger.info(" \n\n CustomAuthenticationSuccessHandler onAuthenticationSuccess User getFirstName: {} ", user.getFirstName());
        logger.info(" \n\n CustomAuthenticationSuccessHandler onAuthenticationSuccess User last name: {} ", user.getLastName());
        response.getOutputStream().write(objectMapper.writeValueAsBytes(user));
    }

}
