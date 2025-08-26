package edu.tamu.scholars.middleware.theme.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
class DeleteActiveThemeExceptionTest {

    @Test
    void testDefaultConstructor() {
        DeleteActiveThemeException exception = new DeleteActiveThemeException("Test delete active theme exception!");
        assertNotNull(exception);
        assertEquals("Test delete active theme exception!", exception.getMessage());
    }

    @Test
    void testThrow() {
        assertThrows(DeleteActiveThemeException.class, () -> {
            throw new DeleteActiveThemeException("Test delete active theme exception!");
        });
    }

}