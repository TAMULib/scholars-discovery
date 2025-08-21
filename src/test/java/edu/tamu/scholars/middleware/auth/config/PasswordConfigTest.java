package edu.tamu.scholars.middleware.auth.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
class PasswordConfigTest {

    @Test
    void testDefaultConstructor() {
        PasswordConfig passwordConfig = new PasswordConfig();
        assertNotNull(passwordConfig);
        assertEquals(180, passwordConfig.getDuration());
        assertEquals(8, passwordConfig.getMinLength());
        assertEquals(64, passwordConfig.getMaxLength());
    }

    @Test
    void testDurationGetterSetter() {
        PasswordConfig passwordConfig = new PasswordConfig();
        passwordConfig.setDuration(90);
        assertEquals(90, passwordConfig.getDuration());
    }

    @Test
    void testMinLengthGetterSetter() {
        PasswordConfig passwordConfig = new PasswordConfig();
        passwordConfig.setMinLength(12);
        assertEquals(12, passwordConfig.getMinLength());
    }

    @Test
    void testMaxLengthGetterSetter() {
        PasswordConfig passwordConfig = new PasswordConfig();
        passwordConfig.setMaxLength(32);
        assertEquals(32, passwordConfig.getMaxLength());
    }

}
