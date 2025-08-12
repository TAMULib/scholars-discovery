package edu.tamu.scholars.middleware.auth.handler;

import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

public class CustomSaml2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    public CustomSaml2AuthenticationSuccessHandler(String defaultTargetUrl) {
        setDefaultTargetUrl(defaultTargetUrl);
        setAlwaysUseDefaultTargetUrl(false);
    }

}
