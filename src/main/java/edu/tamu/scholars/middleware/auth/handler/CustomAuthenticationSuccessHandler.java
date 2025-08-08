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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticatedPrincipal;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import edu.tamu.scholars.middleware.auth.model.User;

/**
 * Spring Boot autoconfigured custom {@link AuthenticationSuccessHandler}. Customized to
 * return authenticated principal as {@link User}.
 */
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    private ObjectMapper objectMapper;

    // @Value("${ui.url:http://localhost:4200}")
    // protected String uiUrl;

    private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();


    public CustomAuthenticationSuccessHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        response.setContentType(APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        Saml2AuthenticatedPrincipal principal = (Saml2AuthenticatedPrincipal) authentication.getPrincipal();
        User user = extractUserFromPrincipal(principal);

        logger.info("Authentication successful. SAMl attribute details: Email={}, FirstName={}, LastName={}",
        user.getEmail(), user.getFirstName(), user.getLastName());

        try {
            redirectStrategy.sendRedirect(request, response, "http://localhost:4200/login-success");
        } catch (Exception e) {
            logger.error("Error writing user data to response", e.getMessage());
            throw new IOException("Failed to write user data to response", e);
        }
    }

    private User extractUserFromPrincipal(@AuthenticationPrincipal Saml2AuthenticatedPrincipal principal) {
        String email = principal.getFirstAttribute("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/emailaddress");
        String firstName = principal.getFirstAttribute("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/givenname");
        String lastName = principal.getFirstAttribute("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/surname");

        User user = new User();
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        return user;
    }

}
