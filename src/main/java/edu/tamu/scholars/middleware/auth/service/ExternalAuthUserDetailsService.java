package edu.tamu.scholars.middleware.auth.service;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * Service interface for loading user details from external authentication providers.
 * 
 * <p>This interface extends the standard Spring Security {@link UserDetailsService} to provide
 * additional support for external authentication mechanisms such as SAML2, OAuth2, LDAP,
 * or other federated identity providers. It enables the application to load or provision
 * user details based on external authentication objects rather than just usernames.</p>
 * 
 * <p>Implementations of this interface are responsible for:</p>
 * <ul>
 *   <li>Extracting user information from external authentication objects</li>
 *   <li>Creating new users when they don't exist locally (user provisioning)</li>
 *   <li>Updating existing user information if needed</li>
 *   <li>Mapping external user attributes to internal user roles and authorities</li>
 *   <li>Handling authentication failures and missing required attributes</li>
 * </ul>
 * 
 * <p>This interface follows the same architectural pattern as {@link UserDetailsService}
 * but operates on external authentication objects instead of simple username strings.
 * It provides a consistent abstraction layer for integrating various external
 * authentication providers into the Spring Security framework.</p>
 * 
 * <p><strong>Implementation Strategy:</strong> Implementations should typically override
 * the inherited {@link UserDetailsService#loadUserByUsername(String)} method to check
 * if a user already exists locally. This method can then be used within
 * {@link #loadUserFromExternalAuthentication(AbstractAuthenticationToken)} to determine
 * whether to create a new user or update existing user attributes from the external
 * authentication source.</p>
 * 
 * <p><strong>Example Usage:</strong></p>
 * <pre>
 * // SAML2 implementation
 * {@code @Service}
 * public class Saml2UserDetailsService 
 *     implements ExternalAuthenticationUserDetailsService&lt;Saml2Authentication&gt; {
 *     
 *     {@code @Override}
 *     public UserDetails loadUserFromExternalAuthentication(Saml2Authentication auth) {
 *         // Extract user attributes from SAML2 assertion
 *         // Create or update user entity
 *         // Return UserDetails implementation
 *     }
 * }
 * 
 * // OAuth2 implementation  
 * {@code @Service}
 * public class OAuth2UserDetailsService 
 *     implements ExternalAuthenticationUserDetailsService&lt;OAuth2Authentication&gt; {
 *     
 *     {@code @Override}
 *     public UserDetails loadUserFromExternalAuthentication(OAuth2Authentication auth) {
 *         // Extract user attributes from OAuth2 token
 *         // Create or update user entity
 *         // Return UserDetails implementation
 *     }
 * }
 * </pre>
 * 
 * @param <E> the type of external authentication token that extends {@link AbstractAuthenticationToken}
 *            (e.g., {@code Saml2Authentication}, {@code BearerTokenAuthentication}, 
 *            {@code JwtAuthenticationToken}, etc.)
 * 
 * @author Texas A&M University Scholars
 * @since 1.0
 * @see UserDetailsService
 * @see AbstractAuthenticationToken
 * @see org.springframework.security.core.Authentication
 * @see org.springframework.security.saml2.provider.service.authentication.Saml2Authentication
 * @see org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication
 */
public interface ExternalAuthUserDetailsService<E extends AbstractAuthenticationToken> extends UserDetailsService {

    /**
     * Loads user details from an external authentication object.
     * 
     * <p>This method extracts user information from the provided external authentication
     * object and returns a {@link UserDetails} implementation. Unlike the standard
     * {@link UserDetailsService#loadUserByUsername(String)} method, this method operates
     * on rich authentication objects that contain user attributes, roles, and other
     * metadata from external identity providers.</p>
     * 
     * <p>Implementations should:</p>
     * <ul>
     *   <li>Validate that all required user attributes are present in the authentication object</li>
     *   <li>Create a new user entity if the user doesn't exist locally (auto-provisioning)</li>
     *   <li>Update existing user information if the external attributes have changed</li>
     *   <li>Map external roles/groups to internal application authorities</li>
     *   <li>Return a fully populated {@link UserDetails} object with appropriate authorities</li>
     * </ul>
     * 
     * <p>The returned {@link UserDetails} object should contain:</p>
     * <ul>
     *   <li>Username (typically extracted from the external authentication)</li>
     *   <li>Enabled/disabled status based on external and internal policies</li>
     *   <li>Account expiration and locking status</li>
     *   <li>Granted authorities mapped from external roles/groups</li>
     * </ul>
     * 
     * @param authentication the external authentication token containing user information
     *                      and attributes from the identity provider
     * @return a fully populated {@link UserDetails} object representing the authenticated user
     * @throws org.springframework.security.core.AuthenticationException if the authentication
     *         object is invalid, missing required attributes, or if user provisioning fails
     * @throws org.springframework.security.authentication.InsufficientAuthenticationException
     *         if required user attributes are missing from the external authentication
     * @throws org.springframework.security.core.userdetails.UsernameNotFoundException
     *         if the user cannot be found and auto-provisioning is disabled
     * 
     * @see UserDetailsService#loadUserByUsername(String)
     * @see UserDetails
     * @see org.springframework.security.core.GrantedAuthority
     */
    UserDetails loadUserFromExternalAuthentication(E authentication);
}
