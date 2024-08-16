package edu.tamu.scholars.middleware.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration class for authentication and authorization settings.
 * 
 * <p>This class is used to load and manage configuration properties related to
 * authentication and authorization from the application's configuration file (e.g.,
 * `src/main/resources/application.yml`). It is marked as a Spring component and
 * is injected into other components as needed.</p>
 * 
 * <p>Configuration properties are prefixed with {@code "middleware.auth"}.</p>
 */
@Component
@ConfigurationProperties(prefix = "middleware.auth")
public class AuthConfig {

    /**
     * Configuration for password settings.
     */
    private PasswordConfig password = new PasswordConfig();

    /**
     * Configuration for token settings.
     */
    private TokenConfig token = new TokenConfig();

    /**
     * Duration (in days) for the registration token validity.
     */
    private int registrationTokenDuration = 14;

    /**
     * Default constructor.
     */
    public AuthConfig() {

    }

    /**
     * Returns the password configuration.

     * @return the {@link PasswordConfig} instance
     */
    public PasswordConfig getPassword() {
        return password;
    }

    /**
     * Sets the password configuration.

     * @param password the {@link PasswordConfig} instance to set
     */
    public void setPassword(PasswordConfig password) {
        this.password = password;
    }

    /**
     * Returns the token configuration.

     * @return the {@link TokenConfig} instance
     */
    public TokenConfig getToken() {
        return token;
    }

    /**
     * Sets the token configuration.

     * @param token the {@link TokenConfig} instance to set
     */
    public void setToken(TokenConfig token) {
        this.token = token;
    }

    /**
     * Returns the duration for the registration token validity.

     * @return the registration token duration in days
     */
    public int getRegistrationTokenDuration() {
        return registrationTokenDuration;
    }

    /**
     * Sets the duration for the registration token validity.

     * @param registrationTokenDuration the duration to set, in days
     */
    public void setRegistrationTokenDuration(int registrationTokenDuration) {
        this.registrationTokenDuration = registrationTokenDuration;
    }
}
