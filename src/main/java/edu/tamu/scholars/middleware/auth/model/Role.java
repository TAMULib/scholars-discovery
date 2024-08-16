package edu.tamu.scholars.middleware.auth.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Enumeration representing user roles for role-based authentication.
 * 
 * <p>This enum defines various roles used in the application for controlling access
 * based on {@link User} roles. The roles are primarily used in {@link SecurityFilterChainConfig}
 * and controller annotations with Spring Security, such as `@PreAuthorize`.</p>
 * 
 * <p>The roles are mapped to their respective string values, which are utilized
 * in security expressions. Note that there is an inconsistency in the use of role
 * keys between the security configuration and the annotation expressions:</p>
 * 
 * <ul>
 * <li>SecurityFilterChainConfig `@PreAuthorize`: `.hasRole("ROLE_USER")`</li>
 * <li>Annotation `@PreAuthorize`: `hasRole('ROLE_USER')`</li>
 * <li>SecurityFilterChainConfig `@PreAuthorize`: `.hasRole("ROLE_ADMIN")`</li>
 * <li>Annotation `@PreAuthorize`: `hasRole('ROLE_ADMIN')`</li>
 * <li>SecurityFilterChainConfig `@PreAuthorize`: `.hasRole("ROLE_SUPER_ADMIN")`</li>
 * <li>Annotation `@PreAuthorize`: `hasRole('ROLE_SUPER_ADMIN')`</li>
 * </ul>
 * 
 * <p>The enum provides methods to get the string value of a role and to retrieve
 * a role instance based on its string value.</p>
 */
public enum Role {

    // NOTE: inconsistent use of enum key
    // SecurityFilterChainConfig @PreAuthorize
    // `.hasRole("USER")`        `@PreAuthorise("hasRole('ROLE_USER')")`
    // `.hasRole("ADMIN")`       `@PreAuthorise("hasRole('ROLE_ADMIN')")`
    // `.hasRole("SUPER_ADMIN")` `@PreAuthorise("hasRole('ROLE_SUPER_ADMIN')")`

    /**
     * Role for general users.
     */
    ROLE_USER("User"),

    /**
     * Role for administrators with elevated privileges.
     */
    ROLE_ADMIN("Administrator"),

    /**
     * Role for super administrators with the highest level of privileges.
     */
    ROLE_SUPER_ADMIN("Super Administrator"); 

    private final String value;

    private static Map<String, Role> map = new HashMap<String, Role>();

    static {
        for (Role role : Role.values()) {
            map.put(role.value, role);
        }
    }

    /**
     * Constructs a new {@code Role} with the specified string value.

     * @param value the string value of the role
     */
    Role(String value) {
        this.value = value;
    }

    /**
     * Returns the string value associated with this role.

     * @return the string value of the role
     */
    public String getValue() {
        return value;
    }

    /**
     * Retrieves the {@code Role} enum constant associated with the specified string value.

     * @param value the string value of the role
     * @return the corresponding {@code Role} enum constant, or {@code null} if no role is found
     */
    public static Role withValue(String value) {
        return map.get(value);
    }

}
