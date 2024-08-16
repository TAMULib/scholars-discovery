package edu.tamu.scholars.middleware.auth.model.repo.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.HandleAfterDelete;
import org.springframework.data.rest.core.annotation.HandleAfterSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import edu.tamu.scholars.middleware.auth.model.User;
import edu.tamu.scholars.middleware.messaging.DeleteEntityMessage;
import edu.tamu.scholars.middleware.messaging.UpdateEntityMessage;

/**
 * Event handler for {@link User} persistence events that dispatches update and delete events via messaging.
 * 
 * <p>This handler listens to save and delete events for {@link User} entities and broadcasts these events to the
 * appropriate messaging channels.</p>
 * 
 * <p>Broadcasts are sent to two channels:</p>
 * <ul>
 * <li>A global channel for all users</li>
 * <li>A user-specific channel based on the email address of the affected user</li>
 * </ul>
 * 
 * <p>The handler uses {@link SimpMessagingTemplate} to send messages. The message format includes details about
 * the updated or deleted entity.</p>
 */
@RepositoryEventHandler(User.class)
public class UserEventHandler {

    /**
     * The destination channel for user-related messages.
     */
    public static final String USERS_CHANNEL = "/queue/users";

    @Autowired
    private SimpMessagingTemplate simpMessageTemplate;

    /**
     * Handles the event after a {@link User} entity is saved. Broadcasts an update message to both the global
     * and user-specific channels.
     * 
     * <p>The broadcast message includes an {@link UpdateEntityMessage} containing the updated user information.</p>

     * @param user the {@link User} entity that has been saved
     */
    @HandleAfterSave
    public void broadcastUserUpdate(User user) {
        simpMessageTemplate.convertAndSend(USERS_CHANNEL, new UpdateEntityMessage<User>(user));
        simpMessageTemplate.convertAndSendToUser(
            user.getEmail(),
            USERS_CHANNEL,
            new UpdateEntityMessage<User>(user)
        );
    }

    /**
     * Handles the event after a {@link User} entity is deleted. Broadcasts a delete message to both the global
     * and user-specific channels.
     * 
     * <p>The broadcast message includes a {@link DeleteEntityMessage}
     *  containing the email address of the deleted user.</p>

     * @param user the {@link User} entity that has been deleted
     */
    @HandleAfterDelete
    public void broadcastUserDelete(User user) {
        simpMessageTemplate.convertAndSend(
            USERS_CHANNEL,
            new DeleteEntityMessage<String>(user.getEmail())
        );
        simpMessageTemplate.convertAndSendToUser(
            user.getEmail(),
            USERS_CHANNEL,
            new DeleteEntityMessage<String>(user.getEmail())
        );
    }

}
