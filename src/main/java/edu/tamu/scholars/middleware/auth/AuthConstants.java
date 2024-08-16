package edu.tamu.scholars.middleware.auth;

import edu.tamu.scholars.middleware.MiddlewareApplication;
import edu.tamu.scholars.middleware.auth.details.CustomUserDetails;

/**
 * This class contains application-wide constants used for authentication-related configurations.
 * 
 * <p>These constants are initialized at runtime and are used statically throughout the application.
 * They are set in {@link MiddlewareApplication} and are referenced in {@link CustomUserDetails}
 * and other authentication components.</p>
 * 
 * <ul>
 *   <li>{@link #PASSWORD_DURATION_IN_DAYS} - Defines the duration, in days, for password validity.</li>
 *   <li>{@link #PASSWORD_MIN_LENGTH} - Specifies the minimum length requirement for passwords.</li>
 *   <li>{@link #PASSWORD_MAX_LENGTH} - Specifies the maximum length limit for passwords.</li>
 * </ul>
 * 
 * <p>The values of these constants should be set appropriately to enforce password policies and manage
 * password expiration in the authentication system.</p>
 */
public class AuthConstants {

    /**
     * Duration in days for which a password is valid before it needs to be changed.
     */
    public static int PASSWORD_DURATION_IN_DAYS;

    /**
     * Minimum length required for a password.
     */
    public static int PASSWORD_MIN_LENGTH;

    /**
     * Maximum length allowed for a password.
     */
    public static int PASSWORD_MAX_LENGTH;

}
