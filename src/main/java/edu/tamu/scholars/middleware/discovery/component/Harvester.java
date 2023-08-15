package edu.tamu.scholars.middleware.discovery.component;

import edu.tamu.scholars.middleware.discovery.model.AbstractIndexDocument;
import edu.tamu.scholars.middleware.discovery.model.Individual;
import reactor.core.publisher.Flux;

public interface Harvester {

    public Flux<Individual> harvest();

    public Individual harvest(String subject);

    public Class<AbstractIndexDocument> type();

}
