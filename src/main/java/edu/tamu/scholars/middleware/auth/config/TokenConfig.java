package edu.tamu.scholars.middleware.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration class for specifying token-related properties.
 * 
 * <p>This class is used to load and manage token configuration settings from the
 * application's configuration file (e.g., `src/main/resources/application.yml`).
 * It is marked as a Spring component and is injected into other components as needed.</p>
 * 
 * <p>Configuration properties are prefixed with 
 * {@code "middleware.auth.token"} in src/main/resources/application.ym.</p>
 */
@Component
@ConfigurationProperties(prefix = "middleware.auth.token")
public class TokenConfig {

    /**
     * An integer value used for server identification or configuration.
     * Default value is 1.
     */
    private int serverInteger = 1;

    /**
     * The server secret used for token generation or validation.
     * Default value is a hardcoded string, but it is recommended to
     * use a secure secret in production environments.
     */
    private String serverSecret = "wKFkxTX54UzKx6xCYnC8WlEI2wtOy0PR";

    /**
     * Number of bytes used for generating pseudo-random numbers for tokens.
     * Default value is 64 bytes.
     */
    private int pseudoRandomNumberBytes = 64;

    /**
     * Default constructor.
     */
    public TokenConfig() {

    }

    /**
     * Returns the integer value used for server identification or configuration.
     * 
     * @return the server integer value
     */
    public int getServerInteger() {
        return serverInteger;
    }

    /**
     * Sets the integer value used for server identification or configuration.
     * 
     * @param serverInteger the integer value to set
     */
    public void setServerInteger(int serverInteger) {
        this.serverInteger = serverInteger;
    }

    /**
     * Returns the server secret used for token generation or validation.
     * 
     * @return the server secret string
     */
    public String getServerSecret() {
        return serverSecret;
    }

    /**
     * Sets the server secret used for token generation or validation.
     * 
     * <p>Note: In a production environment, it is crucial to use a secure
     * and secret value for this property to ensure the security of token-based
     * authentication mechanisms.</p>
     * 
     * @param serverSecret the server secret string to set
     */
    public void setServerSecret(String serverSecret) {
        this.serverSecret = serverSecret;
    }

    /**
     * Returns the number of bytes used for generating pseudo-random numbers for tokens.
     * 
     * @return the number of bytes
     */
    public int getPseudoRandomNumberBytes() {
        return pseudoRandomNumberBytes;
    }

    /**
     * Sets the number of bytes used for generating pseudo-random numbers for tokens.
     * 
     * @param pseudoRandomNumberBytes the number of bytes to set
     */
    public void setPseudoRandomNumberBytes(int pseudoRandomNumberBytes) {
        this.pseudoRandomNumberBytes = pseudoRandomNumberBytes;
    }

}
