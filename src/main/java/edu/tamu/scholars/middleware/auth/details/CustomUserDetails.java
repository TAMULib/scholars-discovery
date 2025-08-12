package edu.tamu.scholars.middleware.auth.details;

import static edu.tamu.scholars.middleware.auth.AuthConstants.PASSWORD_DURATION_IN_DAYS;

import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;

import edu.tamu.scholars.middleware.auth.model.User;

/**
 * Custom {@link UserDetails} that encapsulates the {@link User}.
 */
public class CustomUserDetails extends User implements AuthenticatedPrincipal, UserDetails {

    private static final long serialVersionUID = 6674712962625174202L;

    public CustomUserDetails(User user) {
        super(user);
    }

    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return AuthorityUtils.createAuthorityList(getRole().toString());
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonExpired() {
        return isActive();
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonLocked() {
        return isEnabled() && isConfirmed();
    }

    @Override
    @JsonIgnore
    public boolean isCredentialsNonExpired() {
        return StringUtils.isNotBlank(getPassword())
            && ChronoUnit.DAYS.between(
                getTimestamp().toInstant(),
                Calendar.getInstance().toInstant()
            ) < PASSWORD_DURATION_IN_DAYS;
    }

    @Override
    @JsonIgnore
    public String getUsername() {
        return getEmail();
    }

    @Override
    @JsonIgnore
    public String getName() {
        return getEmail();
    }

}
