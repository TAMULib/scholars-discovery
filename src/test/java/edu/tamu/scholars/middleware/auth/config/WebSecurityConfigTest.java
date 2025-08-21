package edu.tamu.scholars.middleware.auth.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

// @formatter:off
@SpringBootTest(properties = {
    "spring.profiles.active=default",
    "spring.h2.console.enabled=true",
    "solr.host=",
    "solr.repositories.enabled=false"
})
// @formatter:on
class WebSecurityConfigTest {

    @Value("${spring.profiles.active:default}")
    private String profile;

    @Value("${spring.h2.console.enabled:true}")
    private boolean h2ConsoleEnabled;

    @Test
    void testDefaultProfile() {
        assertEquals("default", profile);
    }

    @Test
    void testH2ConsoleEnabled() {
        assertTrue(h2ConsoleEnabled);
    }

}
