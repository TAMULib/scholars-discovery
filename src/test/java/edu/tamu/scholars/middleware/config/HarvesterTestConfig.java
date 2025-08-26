package edu.tamu.scholars.middleware.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import edu.tamu.scholars.middleware.service.TestTriplestore;
import edu.tamu.scholars.middleware.service.Triplestore;

@Configuration
@Profile("test")
class HarvesterTestConfig {

    @Bean
    Triplestore triplestore() {
        return new TestTriplestore();
    }

}
