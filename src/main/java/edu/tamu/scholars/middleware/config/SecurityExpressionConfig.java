package edu.tamu.scholars.middleware.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.security.access.expression.SecurityExpressionHandler;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.messaging.access.expression.DefaultMessageSecurityExpressionHandler;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.expression.DefaultWebSecurityExpressionHandler;

/**
 * 
 */
@Configuration
public class SecurityExpressionConfig {

    @Bean
    public RoleHierarchy roleHierarchy() {
        return RoleHierarchyImpl.fromHierarchy(buildRoleHierarchy());
    }

    @Bean
    public SecurityExpressionHandler<FilterInvocation> securityExpressionHandler() {
        DefaultWebSecurityExpressionHandler seh = new DefaultWebSecurityExpressionHandler();
        seh.setRoleHierarchy(roleHierarchy());

        return seh;
    }

    @Bean
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public SecurityExpressionHandler<Message<Object>> messageSecurityExpressionHandler() {
        DefaultMessageSecurityExpressionHandler seh = new DefaultMessageSecurityExpressionHandler<Message<Object>>();
        seh.setRoleHierarchy(roleHierarchy());

        return seh;
    }

    private String buildRoleHierarchy() {
        return "ROLE_SUPER_ADMIN > ROLE_ADMIN\nROLE_ADMIN > ROLE_USER\n";
    }

}
