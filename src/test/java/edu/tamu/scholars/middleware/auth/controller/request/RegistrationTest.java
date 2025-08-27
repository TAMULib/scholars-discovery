package edu.tamu.scholars.middleware.auth.controller.request;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
class RegistrationTest {

    @Test
    void testDefaultConstructor() {
        Registration registration = new Registration();
        assertNotNull(registration);
    }

    @Test
    void testGettersAndSetters() {
        Registration registration = new Registration();
        registration.setFirstName("Bob");
        registration.setLastName("Boring");
        registration.setEmail("bboring@mailinator.com");
        registration.setPassword("HelloWorld123!");
        registration.setConfirm("HelloWorld123!");
        assertEquals("Bob", registration.getFirstName());
        assertEquals("Boring", registration.getLastName());
        assertEquals("bboring@mailinator.com", registration.getEmail());
        assertEquals("HelloWorld123!", registration.getPassword());
        assertEquals("HelloWorld123!", registration.getConfirm());
    }

}
