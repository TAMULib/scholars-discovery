package edu.tamu.scholars.middleware.auth.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Injectable middleware configuration to specify properties relating to
 * SAML2 authentication and authorization.
 *
 * <p>
 * See `middleware.auth.saml2` in src/main/resources/application.yml.
 * </p>
 */
@Component
@ConfigurationProperties(prefix = "middleware.auth.saml2")
public class Saml2Config {

    private Map<String, String> attributeMap = new HashMap<>();

    public Saml2Config() {

    }

    public Map<String, String> getAttributeMap() {
        return attributeMap;
    }

    public void setAttributeMap(Map<String, String> attributeMap) {
        this.attributeMap = attributeMap;
    }

}
