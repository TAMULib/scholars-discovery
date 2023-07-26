package edu.tamu.scholars.middleware.discovery.service.component;

import java.util.Collection;

import edu.tamu.scholars.middleware.discovery.model.AbstractIndexDocument;

public interface Indexer {

    /**
     * Called to initialize index.
     */
    public void init();

    /**
     * To be called when not needing to initialize index.
     */
    public void scaffold();

    public void index(Collection<AbstractIndexDocument> documents);

    public void index(AbstractIndexDocument document);

    public void optimize();

    public Class<AbstractIndexDocument> type();

    public String name();

}
