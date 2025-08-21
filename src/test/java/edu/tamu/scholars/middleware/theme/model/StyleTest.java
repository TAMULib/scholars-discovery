package edu.tamu.scholars.middleware.theme.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
class StyleTest {

    @Test
    void testDefaultConstructor() {
        Style style = new Style();
        assertNotNull(style);
    }

    @Test
    void testBasicConstructor() {
        Style style = new Style("--variable", "test");
        assertNotNull(style);
        assertEquals("--variable", style.getKey());
        assertEquals("test", style.getValue());
    }

    @Test
    void testGettersAndSetters() {
        Style style = new Style();
        style.setKey("--variable");
        style.setValue("test");
        assertEquals("--variable", style.getKey());
        assertEquals("test", style.getValue());
    }

}
