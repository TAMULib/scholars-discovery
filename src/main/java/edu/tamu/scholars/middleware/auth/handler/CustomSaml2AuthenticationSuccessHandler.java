package edu.tamu.scholars.middleware.auth.handler;

import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

public class CustomSaml2AuthenticationSuccessHandler extends  SavedRequestAwareAuthenticationSuccessHandler  {

    public CustomSaml2AuthenticationSuccessHandler(String defaultTargetUrl) {
        setDefaultTargetUrl(defaultTargetUrl);
        setAlwaysUseDefaultTargetUrl(false);
    }

}
