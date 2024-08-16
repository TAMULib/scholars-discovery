package edu.tamu.scholars.middleware.auth.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.tamu.scholars.middleware.auth.model.User;

/**
 * Controller responsible for handling authentication-related requests.
 * 
 * <p>This controller exposes endpoints to interact with the authenticated {@link User} information.
 * It provides access to the details of the currently authenticated user.</p>
 */
@RestController
public class AuthController {

    /**
     * Handles requests to the "/user" endpoint and returns the currently authenticated {@link User}.
     * 
     * <p>The {@code @AuthenticationPrincipal} annotation is used to inject the currently authenticated
     * user into the method parameter. This method returns the {@link User} object representing the authenticated
     * user details.</p>
     * 
     * @param user the currently authenticated {@link User}
     * @return the {@link User} object representing the authenticated user
     */
    @RequestMapping("/user")
    public User user(@AuthenticationPrincipal User user) {
        return user;
    }

}
