package edu.tamu.scholars.middleware.config;

import java.lang.reflect.InvocationTargetException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import edu.tamu.scholars.middleware.config.model.MiddlewareConfig;
import edu.tamu.scholars.middleware.config.model.TriplestoreConfig;
import edu.tamu.scholars.middleware.service.Triplestore;

/**
 * Externally configured triplestore for harvesting.
 * 
 * <p>See {@link TriplestoreConfig}.</p>
 */
@Configuration
@Profile("!test")
public class HarvesterDependencyConfig {

    @Bean
    Triplestore triplestore(MiddlewareConfig middleware)
        throws InstantiationException, IllegalAccessException, IllegalArgumentException,
        InvocationTargetException, NoSuchMethodException, SecurityException {

        return middleware.getTriplestore()
            .getType()
            .getConstructor(TriplestoreConfig.class)
            .newInstance(middleware.getTriplestore());
    }

}
