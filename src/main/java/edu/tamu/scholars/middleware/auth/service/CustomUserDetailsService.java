package edu.tamu.scholars.middleware.auth.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import edu.tamu.scholars.middleware.auth.details.CustomUserDetails;
import edu.tamu.scholars.middleware.auth.model.User;
import edu.tamu.scholars.middleware.auth.model.repo.UserRepo;

/**
 * Custom implementation of the {@link UserDetailsService} interface for loading
 * user-specific data during authentication.
 * 
 * <p>This service class is responsible for retrieving user details from the database
 * using the {@link UserRepo} repository and returning a {@link CustomUserDetails}
 * instance. It is used by Spring Security to populate the security context with
 * authenticated user details.</p>
 * 
 * <p>If the user is not found in the repository, a {@link UsernameNotFoundException} is thrown
 * with a localized error message.</p>
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private MessageSource messageSource;

    /**
     * Loads user-specific data by the given username.
     * 
     * <p>This method retrieves the user from the {@link UserRepo} repository using the
     * provided username (which is expected to be the user's email address). If the user
     * is found, it returns a {@link CustomUserDetails} instance that wraps the user data.
     * If the user is not found, it throws a {@link UsernameNotFoundException} with a
     * localized error message.</p>
     * 
     * @param username the username (email) of the user to load
     * @return a {@link UserDetails} object containing the user information
     * @throws UsernameNotFoundException if no user is found with the given username
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> user = userRepo.findByEmail(username);
        if (user.isPresent()) {
            return new CustomUserDetails(user.get());
        }
        throw new UsernameNotFoundException(messageSource.getMessage(
                "CustomUserDetailsService.emailNotFound",
                new Object[] { username },
                LocaleContextHolder.getLocale()));
    }

}
