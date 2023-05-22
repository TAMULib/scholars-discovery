package edu.tamu.scholars.middleware.discovery.component.solr;

import static edu.tamu.scholars.middleware.discovery.DiscoveryConstants.COLLECTION;
import static edu.tamu.scholars.middleware.discovery.service.IndexService.CREATED_FIELDS;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicLong;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrResponse;
import org.apache.solr.client.solrj.request.schema.SchemaRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import edu.tamu.scholars.middleware.config.model.IndexConfig;
import edu.tamu.scholars.middleware.discovery.annotation.FieldType;
import edu.tamu.scholars.middleware.discovery.component.Indexer;
import edu.tamu.scholars.middleware.discovery.model.AbstractIndexDocument;

public class SolrIndexer implements Indexer {

    private static final Logger logger = LoggerFactory.getLogger(SolrIndexer.class);

    @Autowired
    private SolrClient solrClient;

    @Autowired
    private IndexConfig indexConfig;

    private final AtomicLong batchId = new AtomicLong(0);

    private final AtomicLong individualCounter = new AtomicLong(0);

    private final Class<AbstractIndexDocument> type;

    public SolrIndexer(Class<AbstractIndexDocument> type) {
        this.type = type;
    }

    @Override
    public void init() {
        for (Field field : FieldUtils.getFieldsListWithAnnotation(type, FieldType.class)) {
            FieldType indexed = field.getAnnotation(FieldType.class);

            String name = StringUtils.isNotEmpty(indexed.value())
                ? indexed.value()
                : field.getName();

            if (!CREATED_FIELDS.contains(name) && CREATED_FIELDS.add(name)) {
                Map<String, Object> fieldAttributes = new HashMap<String,Object>();

                fieldAttributes.put("type", indexed.type());
                fieldAttributes.put("stored", indexed.stored());
                fieldAttributes.put("indexed", indexed.searchable());
                fieldAttributes.put("required", indexed.required());

                if (StringUtils.isNotEmpty(indexed.defaultValue())) {
                    fieldAttributes.put("defaultValue", indexed.defaultValue());
                }

                fieldAttributes.put("multiValued", Collection.class.isAssignableFrom(field.getType()));

                fieldAttributes.put("name", name);

                try {
                    logger.info("Attempting to add field {} with type {} to collection {}", name, indexed.type(), COLLECTION);
                    SchemaRequest.AddField addFieldRequest = new SchemaRequest.AddField(fieldAttributes);
                    SolrResponse response = addFieldRequest.process(solrClient, COLLECTION);
                    logger.info("Committed request to add field {} with type {} to collection {}", name, indexed.type(), COLLECTION);
                    logger.info("Response status: {}", response.getResponse().get("status"));
                } catch (Exception e) {
                    logger.debug("Failed to add field", e);
                }

                if (indexed.copyTo().length > 0) {
                    try {
                        logger.info("Attempting to add copy fields {} from {} to collection {}", indexed.copyTo(), name, indexed.type(), COLLECTION);
                        SchemaRequest.AddCopyField addCopyFieldRequest = new SchemaRequest.AddCopyField(name, Arrays.asList(indexed.copyTo()));
                        SolrResponse response = addCopyFieldRequest.process(solrClient, COLLECTION);
                        logger.info("Committed request to add copy fields {} from {} to collection {}", indexed.copyTo(), name, indexed.type(), COLLECTION);
                        logger.info("Response status: {}", response.getResponse().get("status"));
                    } catch (Exception e) {
                        logger.debug("Failed to add copy field", e);
                    }
                }
            }
        }
    }

    @Override
    public void index(Collection<AbstractIndexDocument> documents) {
        long batchId = this.batchId.addAndGet(1);
        try {
            solrClient.addBeans(COLLECTION, documents);
            solrClient.commit(COLLECTION);
            logger.info("Commit batch index {} {} {}", documents.size(), name(), batchId);
        } catch (Exception e) {
            logger.error("Failed to batch commit {} {} {}", documents.size(), name(), batchId);
            
            if (indexConfig.getResumeIndividually()) {
                logger.info("Resuming {}", indexConfig.getResumeIndividually());
                logger.debug("Resolve stacktrace", e);
                logger.info("Resuming individually");
                documents.stream().forEach(this::index);
            } else {
                logger.info("Enable resuming individual `middleware.index.resumeIndiviudally: true`");
            }
        }
    }

    @Override
    public void index(AbstractIndexDocument document) {
        long batchId = this.batchId.get();
        long individualCounter = this.individualCounter.addAndGet(1);

        try {
            solrClient.addBean(COLLECTION, document);
            solrClient.commit(COLLECTION);
            if (individualCounter % 100 == 0) { // scale down logging by 100 seems reasonable
                logger.info("Saved {} with id {}", name(), document.getId());
                logger.info("Commit individual {} {} of batch {} {}", document.getId(), name(), batchId, individualCounter);
            }
        } catch (Exception e) {
            logger.warn("Failed to commit individual {} {} of batch {} {}", document.getId(), name(), batchId, individualCounter);
            logger.info("Caused by {}", e.getMessage());
        }
    }

    @Override
    public void optimize() {
        try {
            solrClient.optimize(COLLECTION);
        } catch (Exception e) {
            logger.warn("Failed to optimize index", e);
        }
    }

    @Override
    public Class<AbstractIndexDocument> type() {
        return type;
    }

    private String name() {
        return type.getSimpleName();
    }

}
