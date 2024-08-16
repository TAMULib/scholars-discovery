package edu.tamu.scholars.middleware.auth.handler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

/**
 * Custom implementation of {@link LogoutSuccessHandler} for handling successful logout events.
 * 
 * <p>This handler is configured to return a 205 Reset Content status code upon successful logout. It also writes an
 * internationalized (i18n) message to the response body using {@link MessageSource} to support different locales.</p>
 */
public class CustomLogoutSuccessHandler implements LogoutSuccessHandler {

    private MessageSource messageSource;

    /**
     * Constructs a {@link CustomLogoutSuccessHandler} with the specified {@link MessageSource}.
     * 
     * <p>The {@link MessageSource} is used to retrieve the 
     * internationalized message to be included in the response body.</p>

     * @param messageSource the {@link MessageSource} used for retrieving localized messages
     */
    public CustomLogoutSuccessHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    /**
     * Handles successful logout by setting the HTTP response status to 205 (Reset Content)
     * and writing an internationalized message to the response body.
     * 
     * <p>This method retrieves the appropriate localized message from the {@link MessageSource},
     *  writes it to the response, and then flushes and closes the response writer.</p>

     * @param request the HTTP request that triggered the logout
     * @param response the HTTP response to be sent to the client
     * @param authentication the {@link Authentication} object representing the user's authentication details
     * @throws IOException if an input or output error occurs while handling the response
     * @throws ServletException if a servlet error occurs while handling the request
     */
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
