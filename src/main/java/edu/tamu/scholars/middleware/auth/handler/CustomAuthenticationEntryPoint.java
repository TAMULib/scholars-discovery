package edu.tamu.scholars.middleware.auth.handler;

import static jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        response.setStatus(SC_UNAUTHORIZED);
        response.getWriter().write(exception.getMessage());
    }

}
