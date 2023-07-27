package edu.tamu.scholars.middleware.discovery.service.component.solr;

import static edu.tamu.scholars.middleware.discovery.DiscoveryConstants.COLLECTION;
import static edu.tamu.scholars.middleware.discovery.service.IndexService.CREATED_FIELDS;
import static edu.tamu.scholars.middleware.discovery.service.IndexService.SCAFFOLD;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.request.schema.SchemaRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import edu.tamu.scholars.middleware.config.model.IndexConfig;
import edu.tamu.scholars.middleware.discovery.model.AbstractIndexDocument;
import edu.tamu.scholars.middleware.discovery.service.component.Indexer;
import edu.tamu.scholars.middleware.discovery.service.component.NamedTypedField;

public class SolrIndexer implements Indexer {

    private static final Logger logger = LoggerFactory.getLogger(SolrIndexer.class);

    @Autowired
    private SolrClient solrClient;

    @Autowired
    private IndexConfig index;

    private final Class<AbstractIndexDocument> type;

    private final List<NamedTypedField> fields;

    public SolrIndexer(Class<AbstractIndexDocument> type) {
        this.type = type;
        this.fields = new ArrayList<>();
        SCAFFOLD.put(name(), fields);
    }

    @Override
    public void scaffold() {
        try {
            SolrSchemaUtility.collect(type)
                    .filter(ntf -> !ntf.fieldType.readonly())
                    .forEach(ntf -> {
                        fields.add(ntf);
                    });
        } catch (Exception e) {
            logger.debug("Failed to scaffold", e);
        }
    }

    @Override
    public void init(List<Map<String, Object>> schema) {
        if (!index.isInitOnStartup()) {
            return;
        }
        // adding fields and copy fields
        SolrSchemaUtility.collect(type)
                .filter(ntf -> !ntf.fieldType.readonly())
                .filter(ntf -> CREATED_FIELDS.add(ntf.name))
                .forEach(ntf -> {

                    logger.info("Adding field {}.{}", this.name(), ntf.name);

                    // check if field is an existing property in schema
                    SchemaRequest.AddField addFieldRequest = SolrSchemaUtility.addFieldRequest(ntf);

                    try {
                        addFieldRequest.process(solrClient, COLLECTION);
                    } catch (Exception e) {
                        logger.debug("Failed to add field", e);
                    }

                    if (ntf.fieldType.copyTo().length > 0) {
                        logger.info("Adding copy field {}.{} => {}", this.name(), ntf.name, Arrays.asList(ntf.fieldType.copyTo()));
                        SchemaRequest.AddCopyField addCopyFieldRequest = SolrSchemaUtility.addCopyFieldRequest(ntf);
                        try {
                            addCopyFieldRequest.process(solrClient, COLLECTION);
                        } catch (Exception e) {
                            logger.debug("Failed to add copy field", e);
                        }
                    }
                });
    }

    @Override
    public void index(Collection<AbstractIndexDocument> documents) {
        try {
            solrClient.addBeans(COLLECTION, documents);
            solrClient.commit(COLLECTION);
            logger.info(String.format("Saved %s batch of %s", name(), documents.size()));
        } catch (Exception e) {
            logger.warn(String.format("Failed to save batch of %s. Attempting individually.", name()), e);
            if (index.isEnableIndividualOnBatchFail()) {
                documents.stream().forEach(this::index);
            } else {
                logger.warn("Skipping individuals of failed batch of {}.", name());
            }
        }
    }

    @Override
    public void index(AbstractIndexDocument document) {
        try {
            solrClient.addBean(COLLECTION, document);
            solrClient.commit(COLLECTION);
            logger.info(String.format("Saved %s with id %s", name(), document.getId()));
        } catch (Exception e) {
            logger.warn(String.format("Failed to save %s with id %s", name(), document.getId()), e);
        }
    }

    @Override
    public void optimize() {
        try {
            solrClient.optimize(COLLECTION);
        } catch (Exception e) {
            logger.warn(String.format("Failed to optimize index"), e);
        }
    }

    @Override
    public Class<AbstractIndexDocument> type() {
        return type;
    }

    @Override
    public String name() {
        return type.getSimpleName();
    }

}
