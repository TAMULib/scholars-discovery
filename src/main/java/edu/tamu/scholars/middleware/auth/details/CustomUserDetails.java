package edu.tamu.scholars.middleware.auth.details;

import static edu.tamu.scholars.middleware.auth.AuthConstants.PASSWORD_DURATION_IN_DAYS;

import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;

import edu.tamu.scholars.middleware.auth.model.User;

/**
 * Custom implementation of {@link UserDetails} that extends {@link User}.
 * 
 * <p>This class encapsulates a {@link User} object and provides additional details required by the Spring Security
 * framework for authentication and authorization purposes. It implements methods to provide user authorities and
 * handle account status checks.</p>
 */
public class CustomUserDetails extends User implements UserDetails {

    private static final long serialVersionUID = 6674712962625174202L;

    /**
     * Constructs a {@link CustomUserDetails} object from a {@link User}.

     * @param user the {@link User} object to encapsulate
     */
    public CustomUserDetails(User user) {
        super(user);
    }

    /**
     * Returns the authorities granted to the user.
     * 
     * <p>This method provides a collection of granted authorities for the user. It converts the user's role into
     * a list of authorities suitable for Spring Security.</p>

     * @return a collection of {@link GrantedAuthority} representing the user's roles
     */
    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return AuthorityUtils.createAuthorityList(getRole().toString());
    }

    /**
     * Indicates whether the user's account is non-expired.
     * 
     * <p>This method checks if the user's account is still active. In this implementation, it returns whether the
     * user is marked as active.</p>

     * @return {@code true} if the user's account is not expired; {@code false} otherwise
     */
    @Override
    @JsonIgnore
    public boolean isAccountNonExpired() {
        return isActive();
    }

    /**
     * Indicates whether the user's account is non-locked.
     * 
     * <p>This method checks whether the user's account is locked or disabled. It returns {@code true} if the user
     * is enabled and their email is confirmed.</p>

     * @return {@code true} if the user's account is not locked; {@code false} otherwise
     */
    @Override
    @JsonIgnore
    public boolean isAccountNonLocked() {
        return isEnabled() && isConfirmed();
    }

    /**
     * Indicates whether the user's credentials are non-expired.
     * 
     * <p>This method checks whether the user's credentials (e.g., password) are still valid. It compares the current
     * date with the timestamp of when the credentials were last updated, using the password duration defined in the
     * {@code PASSWORD_DURATION_IN_DAYS} constant.</p>

     * @return {@code true} if the user's credentials are not expired; {@code false} otherwise
     */
    @Override
    @JsonIgnore
    public boolean isCredentialsNonExpired() {
        return ChronoUnit.DAYS.between(
            getTimestamp().toInstant(),
            Calendar.getInstance().toInstant()
        ) < PASSWORD_DURATION_IN_DAYS;
    }

    /**
     * Returns the username of the user.
     * 
     * <p>This method returns the email of the user, which is used as the username for authentication purposes.</p>

     * @return the email address of the user
     */
    @Override
    @JsonIgnore
    public String getUsername() {
        return getEmail();
    }

}
