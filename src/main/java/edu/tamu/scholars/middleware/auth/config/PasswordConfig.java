package edu.tamu.scholars.middleware.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration class for specifying password-related properties.
 * 
 * <p>This class is used to load and manage password configuration settings
 * from the application's configuration file (e.g., `src/main/resources/application.yml`).
 * It is marked as a Spring component and is injected into other components as needed.</p>
 * 
 * <p>Configuration properties are prefixed with 
 * {@code "middleware.auth.password"} in src/main/resources/application.yml.</p>
 */
@Component
@ConfigurationProperties(prefix = "middleware.auth.password")
public class PasswordConfig {
    /**
     * The duration (in days) for which the password is considered valid.
     * Default value is 180 days.
     */
    private int duration = 180;

    /**
     * The minimum length required for the password.
     * Default value is 8 characters.
     */
    private int minLength = 8;

    /**
     * The maximum length allowed for the password.
     * Default value is 64 characters.
     */
    private int maxLength = 64;

    /**
     * Default constructor.
     */
    public PasswordConfig() {

    }

    /**
     * Returns the duration (in days) for which the password is considered valid.

     * @return the duration in days
     */
    public int getDuration() {
        return duration;
    }

    /**
     * Sets the duration (in days) for which the password is considered valid.

     * @param duration the duration in days to set
     */
    public void setDuration(int duration) {
        this.duration = duration;
    }

    /**
     * Returns the minimum length required for the password.

     * @return the minimum length
     */
    public int getMinLength() {
        return minLength;
    }

    /**
     * Sets the minimum length required for the password.

     * @param minLength the minimum length to set
     */
    public void setMinLength(int minLength) {
        this.minLength = minLength;
    }

    /**
     * Returns the maximum length allowed for the password.

     * @return the maximum length
     */
    public int getMaxLength() {
        return maxLength;
    }

    /**
     * Sets the maximum length allowed for the password.

     * @param maxLength the maximum length to set
     */
    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }

}
