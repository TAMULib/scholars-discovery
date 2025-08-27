package edu.tamu.scholars.middleware.auth.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

/**
 * Spring Boot autoconfigured custom {@link LogoutSuccessHandler}. Customized to
 * return 205 status and write i18n message to response.
 */
public class CustomLogoutSuccessHandler implements LogoutSuccessHandler {

    private MessageSource messageSource;

    public CustomLogoutSuccessHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Override
    public void onLogoutSuccess(
        HttpServletRequest request,
        HttpServletResponse response,
        Authentication authentication
    ) throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_RESET_CONTENT);
        response.getWriter().write(messageSource.getMessage(
            "CustomLogoutSuccessHandler.success",
            new Object[0],
            LocaleContextHolder.getLocale()
        ));
        response.getWriter().flush();
        response.getWriter().close();
    }

}
