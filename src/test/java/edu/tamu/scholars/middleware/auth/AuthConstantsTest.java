package edu.tamu.scholars.middleware.auth;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
class AuthConstantsTest {

    @Test
    void testDefaultConstructor() {
        AuthConstants authConstants = new AuthConstants();
        assertNotNull(authConstants);
    }

}
