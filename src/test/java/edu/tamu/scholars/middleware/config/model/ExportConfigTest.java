package edu.tamu.scholars.middleware.config.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
class ExportConfigTest {

    @Test
    void testDefaultConstructor() {
        ExportConfig exportConfig = new ExportConfig();
        assertNotNull(exportConfig);
        assertEquals("individual", exportConfig.getIndividualKey());
        assertEquals("http://localhost:4200/display", exportConfig.getIndividualBaseUri());
    }

    @Test
    void testIndividualKeyGetterSetter() {
        ExportConfig exportConfig = new ExportConfig();
        exportConfig.setIndividualKey("link");
        assertEquals("link", exportConfig.getIndividualKey());
    }

    @Test
    void testIndividualBaseUriGetterSetter() {
        ExportConfig exportConfig = new ExportConfig();
        exportConfig.setIndividualBaseUri("http://localhost:8080/vivo/display");
        assertEquals("http://localhost:8080/vivo/display", exportConfig.getIndividualBaseUri());
    }

}
