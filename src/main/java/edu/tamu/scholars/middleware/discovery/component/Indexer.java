package edu.tamu.scholars.middleware.discovery.component;

import java.util.Collection;

import edu.tamu.scholars.middleware.discovery.model.AbstractIndexDocument;
import edu.tamu.scholars.middleware.discovery.model.Individual;

public interface Indexer {

    public void init();

    public void index(Collection<Individual> documents);

    public void index(Individual document);

    public void optimize();

    public Class<AbstractIndexDocument> type();

}
