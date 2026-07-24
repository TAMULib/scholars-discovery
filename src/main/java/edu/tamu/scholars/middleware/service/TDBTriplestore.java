package edu.tamu.scholars.middleware.service;

import java.time.Duration;
import java.time.Instant;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.tdb1.TDB1;
import org.apache.jena.tdb1.TDB1Factory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.tamu.scholars.middleware.config.model.TriplestoreConfig;

/**
 * 
 */
//CHECKSTYLE.OFF: AbbreviationAsWordInName - TDBFactory
public class TDBTriplestore implements Triplestore {
    //CHECKSTYLE.ON: AbbreviationAsWordInName

    private static final Logger logger = LoggerFactory.getLogger(TDBTriplestore.class);

    private final TriplestoreConfig config;

    private Dataset dataset;

    public TDBTriplestore(TriplestoreConfig config) {
        this.config = config;
    }

    @Override
    public QueryExecution createQueryExecution(String query) {
        return QueryExecutionFactory.create(query, dataset);
    }

    @Override
    public void init() {
        final Instant start = Instant.now();
        logger.info("Intializing {}", config.getType().getSimpleName());
        // TODO: handle missing configurations
        TDB1.getContext().setTrue(TDB1.symUnionDefaultGraph);
        dataset = TDB1Factory.createDataset(config.getDirectory());
        logger.info(
            "{} ready. {} seconds",
            config.getType().getSimpleName(),
            Duration.between(start, Instant.now()).toMillis() / 1000.0
        );
    }

    @Override
    public void destroy() {
        dataset.close();
    }

}
