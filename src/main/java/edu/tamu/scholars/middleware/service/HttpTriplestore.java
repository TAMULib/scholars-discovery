package edu.tamu.scholars.middleware.service;

import java.time.Duration;
import java.time.Instant;

import org.apache.jena.query.QueryExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.tamu.scholars.middleware.config.model.TriplestoreConfig;

public class HttpTriplestore implements Triplestore {

    private final static Logger logger = LoggerFactory.getLogger(HttpTriplestore.class);

    private final TriplestoreConfig config;

    public HttpTriplestore(TriplestoreConfig config) {
        this.config = config;
    }

    @Override
    public QueryExecution createQueryExecution(String query) {
        return QueryExecution.service(config.getDatasourceUrl()).query(query).build();
    }

    @Override
    public void init() {
        Instant start = Instant.now();
        logger.info("Initializing {}", config.getType().getSimpleName());
        logger.info("{} ready. {} seconds", config.getType().getSimpleName(), Duration.between(start, Instant.now()).toMillis() / 1000.0);
    }

    @Override
    public void destroy() {
        // no-op
    }

}
