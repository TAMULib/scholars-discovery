package edu.tamu.scholars.middleware.config.model;

import java.util.ArrayList;
import java.util.List;

import edu.tamu.scholars.middleware.discovery.model.AbstractIndexDocument;

/**
 * Inherited by {@link HarvesterConfig} and {@link IndexerConfig}.
 */
public abstract class IndexDocumentTypesConfig {

    private List<Class<? extends AbstractIndexDocument>> documentTypes = new ArrayList<>();

    protected IndexDocumentTypesConfig() {

    }

    public List<Class<? extends AbstractIndexDocument>> getDocumentTypes() {
        return documentTypes;
    }

    public void setDocumentTypes(List<Class<? extends AbstractIndexDocument>> documentTypes) {
        this.documentTypes = documentTypes;
    }

}
