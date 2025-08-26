package edu.tamu.scholars.middleware.auth.service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticatedPrincipal;
import org.springframework.security.saml2.provider.service.authentication.Saml2Authentication;
import org.springframework.stereotype.Service;

import edu.tamu.scholars.middleware.auth.config.AuthConfig;
import edu.tamu.scholars.middleware.auth.details.CustomUserDetails;
import edu.tamu.scholars.middleware.auth.model.Role;
import edu.tamu.scholars.middleware.auth.model.User;
import edu.tamu.scholars.middleware.auth.model.repo.UserRepo;

/**
 * Spring Boot autoconfigured custom {@link UserDetailsService}. Customized to
 * load a {@link CustomUserDetails} from a persisted {@link User} into the
 * Spring security context for an authenticated principle.
 */
@Service
public class CustomUserDetailsService implements ExternalAuthUserDetailsService<Saml2Authentication> {

    private static final String FIRST_NAME_KEY = "firstName";
    private static final String LAST_NAME_KEY = "lastName";
    private static final String USERNAME_KEY = "username";

    private final AuthConfig authConfig;
    private final UserRepo userRepo;
    private final MessageSource messageSource;

    public CustomUserDetailsService(AuthConfig authConfig, UserRepo userRepo, MessageSource messageSource) {
        this.authConfig = authConfig;
        this.userRepo = userRepo;
        this.messageSource = messageSource;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> user = userRepo.findByEmail(username);
        if (user.isPresent()) {
            return new CustomUserDetails(user.get());
        }
        throw new UsernameNotFoundException(messageSource.getMessage(
                "CustomUserDetailsService.emailNotFound",
                new Object[] { username },
                LocaleContextHolder.getLocale()));
    }

    /**
     * Creates and persists a new user from SAML2 authentication attributes.
     * 
     * <p>Maps SAML2 attributes to user properties using configurable attribute mappings.
     * The attribute map allows flexible mapping between SAML2 claim names and internal
     * user properties.</p>
     * 
     * <p>Required attribute mappings (with example SAML2 claim URIs):</p>
     * <ul>
     *   <li><strong>firstName</strong>: Maps to user's first name 
     *       (e.g., "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/givenname")</li>
     *   <li><strong>lastName</strong>: Maps to user's last name 
     *       (e.g., "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/surname")</li>
     *   <li><strong>username</strong>: Maps to user's username 
     *       (e.g., "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/name")</li>
     * </ul>
     * 
     * <p>Example YAML configuration:</p>
     * <pre>
     * middleware
     *   saml2:
     *     attributeMap:
     *       firstName: http://schemas.xmlsoap.org/ws/2005/05/identity/claims/givenname
     *       lastName: http://schemas.xmlsoap.org/ws/2005/05/identity/claims/surname
     *       username: http://schemas.xmlsoap.org/ws/2005/05/identity/claims/name
     * </pre>
     * 
     * @param authentication the SAML2 authentication object containing user attributes
     * @return UserDetails wrapping the newly created and persisted user
     * @throws InsufficientAuthenticationException if any required SAML2 attributes are missing or blank
     */
    public UserDetails loadUserFromExternalAuthentication(Saml2Authentication authentication) {
        Saml2AuthenticatedPrincipal principal = (Saml2AuthenticatedPrincipal) authentication.getPrincipal();
        Map<String, String> attributeMap = this.authConfig.getSaml2().getAttributeMap();
        Map<String, List<Object>> attributes = principal.getAttributes();

        String firstName = extractRequiredAttribute(attributes, attributeMap, FIRST_NAME_KEY);
        String lastName = extractRequiredAttribute(attributes, attributeMap, LAST_NAME_KEY);
        String username = extractRequiredAttribute(attributes, attributeMap, USERNAME_KEY);

        User user = createUserFromAttributes(firstName, lastName, username);

        return new CustomUserDetails(userRepo.save(user));
    }

    /**
     * Extracts a required attribute value from SAML2 attributes using the configured mapping.
     * 
     * @param attributes the SAML2 attributes map
     * @param attributeMap the configured attribute mapping
     * @param attributeKey the internal attribute key to extract
     * @return the extracted attribute value
     * @throws InsufficientAuthenticationException if the attribute is missing or blank
     */
    private String extractRequiredAttribute(Map<String, List<Object>> attributes, 
                                        Map<String, String> attributeMap, 
                                        String attributeKey) {
        String samlAttributeKey = attributeMap.getOrDefault(attributeKey, attributeKey);
        List<Object> attributeValues = attributes.getOrDefault(samlAttributeKey, Collections.emptyList());
        
        if (attributeValues.isEmpty() || StringUtils.isBlank(attributeValues.get(0).toString())) {
            throw new InsufficientAuthenticationException(
                String.format("SAML2 authentication response is missing required '%s' attribute", samlAttributeKey));
        }

        return attributeValues.get(0).toString();
    }

    /**
     * Creates a new User entity with the provided attributes and default settings.
     * 
     * Set's external authenticated user active, confirmed, and enabled with user role.
     * 
     * @param firstName the user's first name
     * @param lastName the user's last name
     * @param username the user's username
     * @return a new User entity with default active/confirmed/enabled status and ROLE_USER role
     */
    private User createUserFromAttributes(String firstName, String lastName, String username) {
        User user = new User(firstName, lastName, username);
        user.setActive(true);
        user.setConfirmed(true);
        user.setEnabled(true);
        user.setRole(Role.ROLE_USER);

        return user;
    }

}
