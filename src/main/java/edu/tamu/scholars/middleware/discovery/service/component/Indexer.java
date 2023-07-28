package edu.tamu.scholars.middleware.discovery.service.component;

import java.util.Collection;
import java.util.Map;

import edu.tamu.scholars.middleware.discovery.model.AbstractIndexDocument;

/**
 * Indexer interface so far. See solr.SolrIndexer ^^^ and ..IndexService <<<.
 */
public interface Indexer {

    /**
     * Scaffold in memory fields expected from concrete discovery.model.
     */
    public void scaffold();

    /**
     * Everything the application needs the solr collection to have specified.
     */
    public void init(Map<String, Object> schema);

    /**
     * Index a batch of abstract index documents.
     * 
     * @param documents batch to index
     */
    public void index(Collection<AbstractIndexDocument> documents);

    /**
     * Index an abstract index documents.
     *
     * @param document individual
     */
    public void index(AbstractIndexDocument document);

    /**
     * Used to flush commits.
     */
    public void optimize();

    /**
     * Reflected type.
     * 
     * @return the typed class for abstract index document
     */
    public Class<AbstractIndexDocument> type();

    /**
     * The concrete name provided by the implementation. Please place in discovery level constants class as final static.
     * 
     * @return name for the implementation
     */
    public String name();

}
