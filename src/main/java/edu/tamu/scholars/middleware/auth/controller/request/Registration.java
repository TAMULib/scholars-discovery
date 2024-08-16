package edu.tamu.scholars.middleware.auth.controller.request;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonInclude;

import edu.tamu.scholars.middleware.auth.annotation.AvailableEmail;
import edu.tamu.scholars.middleware.auth.annotation.ValidPassword;
import edu.tamu.scholars.middleware.auth.model.User;
import edu.tamu.scholars.middleware.auth.validator.group.CompleteRegistration;
import edu.tamu.scholars.middleware.auth.validator.group.SubmitRegistration;

/**
 * Data Transfer Object (DTO) for user registration requests.
 * 
 * <p>This class is used to encapsulate the data required for {@link User} registration
 * and includes validation annotations for different stages of registration: submission
 * and completion.</p>
 * 
 * <p>The {@link ValidPassword} annotation ensures that the password meets specific
 * validation criteria during complete registration.</p>
 * 
 * <p>The validation constraints include size limits for names, mandatory email checks,
 * email format validation, and ensuring that the email is not already in use.</p>
 */
@ValidPassword(groups = CompleteRegistration.class, message = "{Registration.passwordInvalid}")
public class Registration {

    /**
     * The user's first name.
     * 
     * <p>Must be between 2 and 64 characters in length for both submission and completion
     * registration stages.</p>
     */
    @Size(
        min = 2,
        max = 64,
        groups = {
            SubmitRegistration.class,
            CompleteRegistration.class
        },
        message = "{Registration.firstNameSize}"
    )
    private String firstName;

    /**
     * The user's last name.
     * 
     * <p>Must be between 2 and 64 characters in length for both submission and completion
     * registration stages.</p>
     */
    @Size(
        min = 2,
        max = 64,
        groups = {
            SubmitRegistration.class,
            CompleteRegistration.class
        },
        message = "{Registration.lastNameSize}"
    )
    private String lastName;

    /**
     * The user's email address.
     * 
     * <p>Must not be null or empty and must be a valid email format. It also must be unique
     * during submission registration.</p>
     */
    @NotNull(
        message = "{Registration.emailRequired}",
        groups = {
            SubmitRegistration.class,
            CompleteRegistration.class
        }
    )
    @NotEmpty(
        message = "{Registration.emailRequired}",
        groups = {
            SubmitRegistration.class,
            CompleteRegistration.class
        }
    )
    @AvailableEmail(
        groups = SubmitRegistration.class,
        message = "{Registration.emailAlreadyInUse}"
    )
    @Email(
        message = "{Registration.emailInvalid}",
        groups = {
            SubmitRegistration.class,
            CompleteRegistration.class
        }
    )
    private String email;

    /**
     * The user's password.
     * 
     * <p>This field is optional and is only included in the JSON if it is not null. Used
     * for user registration when completing the registration process.</p>
     */
    @JsonInclude(NON_NULL)
    private String password;

    /**
     * The user's password confirmation.
     * 
     * <p>This field is optional and is only included in the JSON if it is not null. It is
     * used to confirm that the password entered by the user matches the confirmation during
     * registration completion.</p>
     */
    @JsonInclude(NON_NULL)
    private String confirm;

    /**
     * Default constructor for the {@code Registration} class.
     * 
     * <p>This constructor initializes a new instance of the {@code Registration} class.</p>
     */
    public Registration() {
        super();
    }

    /**
     * Gets the user's first name.

     * @return the first name of the user
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Sets the user's first name.

     * @param firstName the first name of the user
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Gets the user's last name.

     * @return the last name of the user
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Sets the user's last name.

     * @param lastName the last name of the user
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Gets the user's email address.

     * @return the email address of the user
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the user's email address.

     * @param email the email address of the user
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Gets the user's password.

     * @return the password of the user
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the user's password.

     * @param password the password of the user
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Gets the user's password confirmation.

     * @return the password confirmation of the user
     */
    public String getConfirm() {
        return confirm;
    }

    /**
     * Sets the user's password confirmation.

     * @param confirm the password confirmation of the user
     */
    public void setConfirm(String confirm) {
        this.confirm = confirm;
    }

}
