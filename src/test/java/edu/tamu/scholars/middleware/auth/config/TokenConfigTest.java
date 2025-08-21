package edu.tamu.scholars.middleware.auth.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
class TokenConfigTest {

    @Test
    void testDefaultConstructor() {
        TokenConfig tokenConfig = new TokenConfig();
        assertNotNull(tokenConfig);
        assertEquals(1, tokenConfig.getServerInteger());
        assertEquals("wKFkxTX54UzKx6xCYnC8WlEI2wtOy0PR", tokenConfig.getServerSecret());
    }

    @Test
    void testServerIntegerGetterSetter() {
        TokenConfig tokenConfig = new TokenConfig();
        tokenConfig.setServerInteger(2);
        assertEquals(2, tokenConfig.getServerInteger());
    }

    @Test
    void testServerSecretGetterSetter() {
        TokenConfig tokenConfig = new TokenConfig();
        tokenConfig.setServerSecret("ulqj2hIqUNbJdTvl1QEeB398XZlhgO3D");
        assertEquals("ulqj2hIqUNbJdTvl1QEeB398XZlhgO3D", tokenConfig.getServerSecret());
    }

    @Test
    void testPseudoRandomNumberBytesGetterSetter() {
        TokenConfig tokenConfig = new TokenConfig();
        tokenConfig.setPseudoRandomNumberBytes(128);
        assertEquals(128, tokenConfig.getPseudoRandomNumberBytes());
    }

}
