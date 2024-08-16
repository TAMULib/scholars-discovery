package edu.tamu.scholars.middleware.auth.model;

import static com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY;
import static javax.persistence.EnumType.STRING;
import static javax.persistence.FetchType.EAGER;
import static javax.persistence.GenerationType.IDENTITY;
import static javax.persistence.TemporalType.TIMESTAMP;
import static org.springframework.beans.BeanUtils.copyProperties;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import edu.tamu.scholars.middleware.auth.details.CustomUserDetails;

/**
 * Represents a user in the application.
 * 
 * <p>The {@link User} entity is used to identify, authenticate, and authorize users within the application.
 * It is integrated with Spring Security through the {@link CustomUserDetails} class. This class contains
 * user details including personal information, authentication credentials, and role-based permissions.</p>
 * 
 * <p>The user entity is mapped to the database table "users" and includes fields for user identification, 
 * authentication, and account management.</p>
 */
@Entity
@Table(name = "users")
public class User implements Serializable {

    private static final long serialVersionUID = -7535464109980348619L;

    @Id
    @JsonInclude(Include.NON_EMPTY)
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @NotNull(message = "${User.firstNameRequired}")
    @Size(min = 2, max = 64, message = "${User.firstNameSize}")
    @Column(nullable = false)
    private String firstName;

    @NotNull(message = "${User.lastNameRequired}")
    @Size(min = 2, max = 64, message = "${User.lastNameSize}")
    @Column(nullable = false)
    private String lastName;

    @NotNull(message = "{User.emailRequired}")
    @Email(message = "{User.emailInvalid}")
    @Column(nullable = false, unique = true)
    private String email;

    @JsonProperty(access = WRITE_ONLY)
    @Column
    private String password;

    @JsonIgnore
    @ElementCollection(fetch = EAGER)
    private List<String> oldPasswords;

    @NotNull(message = "{User.roleRequired}")
    @Enumerated(STRING)
    @Column(nullable = false)
    private Role role;

    @JsonIgnore
    @CreationTimestamp
    @Temporal(TIMESTAMP)
    @Column(nullable = false)
    private Calendar created;

    @JsonIgnore
    @UpdateTimestamp
    @Temporal(TIMESTAMP)
    @Column(nullable = false)
    private Calendar timestamp;

    @JsonIgnore
    @Column(nullable = false)
    private boolean confirmed;

    @Column(nullable = false)
    private boolean active;

    @Column(nullable = false)
    private boolean enabled;

    /**
     * Default constructor.
     * 
     * <p>Initializes the user's old passwords list, role, creation and update timestamps,
     * confirmation status, active status, and enabled status with default values.</p>
     */
    public User() {
        super();
        this.oldPasswords = new ArrayList<String>();
        this.role = Role.ROLE_USER;
        this.created = Calendar.getInstance();
        this.timestamp = Calendar.getInstance();
        this.confirmed = false;
        this.active = true;
        this.enabled = false;
    }

    /**
     * Constructs a new {@code User} with the specified first name, last name, and email.
     * 
     * @param firstName the user's first name
     * @param lastName  the user's last name
     * @param email     the user's email address
     */
    public User(String firstName, String lastName, String email) {
        this();
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }

    /**
     * Constructs a new {@code User} by copying properties from another {@code User} instance.
     * 
     * @param user the {@code User} instance to copy properties from
     */
    public User(User user) {
        this();
        copyProperties(user, this);
    }

    /**
     * Returns the unique identifier of the user.
     * 
     * @return the user's ID
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the unique identifier of the user.
     * 
     * @param id the user's ID
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Returns the user's first name.
     * 
     * @return the user's first name
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Sets the user's first name.
     * 
     * @param firstName the user's first name
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Returns the user's last name.
     * 
     * @return the user's last name
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Sets the user's last name.
     * 
     * @param lastName the user's last name
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Returns the user's email address.
     * 
     * @return the user's email address
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the user's email address.
     * 
     * @param email the user's email address
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Returns the user's password.
     * 
     * @return the user's password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the user's password.
     * 
     * @param password the user's password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Returns the list of old passwords for the user.
     * 
     * @return the list of old passwords
     */
    public List<String> getOldPasswords() {
        return oldPasswords;
    }

    /**
     * Sets the list of old passwords for the user.
     * 
     * @param oldPasswords the list of old passwords
     */
    public void setOldPasswords(List<String> oldPasswords) {
        this.oldPasswords = oldPasswords;
    }

    /**
     * Returns the role of the user.
     * 
     * @return the user's role
     */
    public Role getRole() {
        return role;
    }

    /**
     * Sets the role of the user.
     * 
     * @param role the user's role
     */
    public void setRole(Role role) {
        this.role = role;
    }

    /**
     * Returns the timestamp of when the user was created.
     * 
     * @return the creation timestamp
     */
    public Calendar getCreated() {
        return created;
    }

    /**
     * Sets the timestamp of when the user was created.
     * 
     * @param created the creation timestamp
     */
    public void setCreated(Calendar created) {
        this.created = created;
    }

    /**
     * Returns the timestamp of the last update to the user record.
     * 
     * @return the last update timestamp
     */
    public Calendar getTimestamp() {
        return timestamp;
    }

    /**
     * Sets the timestamp of the last update to the user record.
     * 
     * @param timestamp the last update timestamp
     */
    public void setTimestamp(Calendar timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Returns whether the user's email has been confirmed.
     * 
     * @return {@code true} if the email is confirmed; {@code false} otherwise
     */
    public boolean isConfirmed() {
        return confirmed;
    }

    /**
     * Sets whether the user's email has been confirmed.
     * 
     * @param confirmed {@code true} if the email is confirmed; {@code false} otherwise
     */
    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }

    /**
     * Returns whether the user's account is active.
     * 
     * @return {@code true} if the account is active; {@code false} otherwise
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Sets whether the user's account is active.
     * 
     * @param active {@code true} if the account is active; {@code false} otherwise
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * Returns whether the user's account is enabled.
     * 
     * @return {@code true} if the account is enabled; {@code false} otherwise
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Sets whether the user's account is enabled.
     * 
     * @param enabled {@code true} if the account is enabled; {@code false} otherwise
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

}