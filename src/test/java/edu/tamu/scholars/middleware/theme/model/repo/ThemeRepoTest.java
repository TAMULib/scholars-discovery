package edu.tamu.scholars.middleware.theme.model.repo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import edu.tamu.scholars.middleware.config.model.MiddlewareConfig;
import edu.tamu.scholars.middleware.theme.ThemeIntegrationTest;
import edu.tamu.scholars.middleware.theme.model.Theme;

@DataJpaTest
class ThemeRepoTest extends ThemeIntegrationTest {

    @TestConfiguration
    static class ThemeRepoTestContextConfiguration {

        @Bean
        MiddlewareConfig middlewareConfig() {
            return new MiddlewareConfig();
        }

        @Bean
        BCryptPasswordEncoder bCryptPasswordEncoder() {
            return new BCryptPasswordEncoder();
        }

    }

    @Test
    void testCreate() {
        assertEquals(0, themeRepo.count());
        Theme theme = getMockTheme();
        themeRepo.save(theme);
        assertEquals(1, themeRepo.count());
    }

    @Test
    void testRead() {
        testCreate();
        Optional<Theme> theme = themeRepo.findByName("Test");
        assertTrue(theme.isPresent());
    }

    @Test
    void testUpdate() {
        testCreate();
        Optional<Theme> theme = themeRepo.findByName("Test");
        theme.get().setActive(true);

        themeRepo.save(theme.get());

        assertEquals(1, themeRepo.count());

        theme = themeRepo.findByName("Test");
        assertEquals("Test", theme.get().getName());
        assertTrue(theme.get().isActive());
    }

    @Test
    void testDelete() {
        testCreate();
        assertEquals(1, themeRepo.count());
        Optional<Theme> theme = themeRepo.findByName("Test");
        themeRepo.delete(theme.get());
        assertEquals(0, themeRepo.count());
    }

}
