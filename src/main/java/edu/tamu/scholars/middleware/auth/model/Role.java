package edu.tamu.scholars.middleware.auth.model;

import java.util.HashMap;
import java.util.Map;

/**
 * {@link User} role enumeration used for role-based authentication defined
 * primarily in the {@link SecurityFilterChainConfig} or controller
 * annotations @PreAuthorize using spring expression `hasRole`.
 */
public enum Role {

    // NOTE: inconsistent use of enum key
    // SecurityFilterChainConfig @PreAuthorize
    // `.hasRole("USER")`        `@PreAuthorise("hasRole('ROLE_USER')")`
    // `.hasRole("ADMIN")`       `@PreAuthorise("hasRole('ROLE_ADMIN')")`
    // `.hasRole("SUPER_ADMIN")` `@PreAuthorise("hasRole('ROLE_SUPER_ADMIN')")`

    ROLE_USER("User"),
    ROLE_ADMIN("Administrator"),
    ROLE_SUPER_ADMIN("Super Administrator"); 

    private final String value;

    private static Map<String, Role> map = new HashMap<>();

    static {
        for (Role role : Role.values()) {
            map.put(role.value, role);
        }
    }

    Role(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static Role withValue(String value) {
        return map.get(value);
    }

}
