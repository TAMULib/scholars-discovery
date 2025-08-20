package edu.tamu.scholars.middleware.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;

/**
 * i18n configuration beans. Creates {@link MessageSource} bean to provide i18n keys.
 */
@Configuration
public class InternationalizationConfig {

    @Bean
    MessageSource messageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.addBasenames("i18n/messages");
        return messageSource;
    }

}
