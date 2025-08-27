package edu.tamu.scholars.middleware.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Injectable middleware configuration to specify properties relating to
 * authentication and authorization.
 *
 * <p>
 * See `middleware.auth` in src/main/resources/application.yml.
 * </p>
 */
@Component
@ConfigurationProperties(prefix = "middleware.auth")
public class AuthConfig {

    private PasswordConfig password = new PasswordConfig();

    private TokenConfig token = new TokenConfig();

    private Saml2Config saml2 = new Saml2Config();

    private int registrationTokenDuration = 14;

    public PasswordConfig getPassword() {
        return password;
    }

    public void setPassword(PasswordConfig password) {
        this.password = password;
    }

    public TokenConfig getToken() {
        return token;
    }

    public void setToken(TokenConfig token) {
        this.token = token;
    }

    public Saml2Config getSaml2() {
        return saml2;
    }

    public void setSaml2(Saml2Config saml2) {
        this.saml2 = saml2;
    }

    public int getRegistrationTokenDuration() {
        return registrationTokenDuration;
    }

    public void setRegistrationTokenDuration(int registrationTokenDuration) {
        this.registrationTokenDuration = registrationTokenDuration;
    }

}
