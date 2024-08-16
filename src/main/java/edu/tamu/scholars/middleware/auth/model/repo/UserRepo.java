package edu.tamu.scholars.middleware.auth.model.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RestResource;

import edu.tamu.scholars.middleware.auth.model.User;

/**
 * Repository interface for {@link User} entities.
 * 
 * <p>This interface extends {@link JpaRepository} to provide CRUD operations on {@link User} entities
 * with {@code Long} as the identifier type.</p>
 * 
 * <p>Additionally, it includes custom query methods to find a user by their email and check if an email exists
 * in the system. These methods are suppressed from being exposed through Spring Data REST controller endpoints.</p>
 * 
 * <p>Methods:
 * <ul>
 * <li>{@link #findByEmail(String)}: Retrieves a {@link User} by their email address if present.</li>
 * <li>{@link #existsByEmail(String)}: Checks if a {@link User} with the specified email address exists.</li>
 * </ul>
 * </p>
 */
public interface UserRepo extends JpaRepository<User, Long> {

    /**
     * Finds a {@link User} entity by the specified email address.
     * 
     * <p>This method is suppressed from Spring Data REST controller endpoints using {@link RestResource}.</p>
     * 
     * @param email the email address of the {@link User} to find
     * @return an {@link Optional} containing the {@link User} if found, or an empty {@link Optional} if not found
     */
    @RestResource(exported = false)
    public Optional<User> findByEmail(String email);

    /**
     * Checks if a {@link User} entity exists with the specified email address.
     * 
     * <p>This method is suppressed from Spring Data REST controller endpoints using {@link RestResource}.</p>
     * 
     * @param email the email address to check
     * @return {@code true} if a {@link User} with the specified email exists, {@code false} otherwise
     */
    @RestResource(exported = false)
    public boolean existsByEmail(String email);

}
