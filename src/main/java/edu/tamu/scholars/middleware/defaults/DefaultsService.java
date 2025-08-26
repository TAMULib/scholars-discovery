package edu.tamu.scholars.middleware.defaults;

import java.io.IOException;
import java.util.List;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import edu.tamu.scholars.middleware.config.model.MiddlewareConfig;

/**
 * 
 */
@Service
@Profile("!test")
public class DefaultsService {

    private final MiddlewareConfig middleware;
    private final List<Defaults<?, ?>> defaults;

    public DefaultsService(MiddlewareConfig middleware, List<Defaults<?, ?>> defaults) {
        this.middleware = middleware;
        this.defaults = defaults;
    }

    @PostConstruct
    public void init() throws IOException {
        if (middleware.isLoadDefaults()) {
            for (Defaults<?, ?> service : defaults) {
                service.load();
            }
        }
    }

}
