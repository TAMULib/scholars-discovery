package edu.tamu.scholars.middleware.discovery.component.solr;

import static edu.tamu.scholars.middleware.discovery.DiscoveryConstants.COLLECTION;
import static edu.tamu.scholars.middleware.discovery.service.IndexService.CREATED_FIELDS;

import static java.lang.String.format;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicLong;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.solr.client.solrj.SolrClient;
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

            if (!indexed.readonly() && !CREATED_FIELDS.contains(name) && CREATED_FIELDS.add(name)) {
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
                    SchemaRequest.AddField addFieldRequest = new SchemaRequest.AddField(fieldAttributes);
                    addFieldRequest.process(solrClient, COLLECTION);
                } catch (Exception e) {
                    logger.debug("Failed to add field", e);
                }

                if (indexed.copyTo().length > 0) {
                    try {
                        SchemaRequest.AddCopyField addCopyFieldRequest = new SchemaRequest.AddCopyField(name, Arrays.asList(indexed.copyTo()));
                        addCopyFieldRequest.process(solrClient, COLLECTION);
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
            logger.info(format("Commit batch index %s %s %s", documents.size(), name(), batchId));
        } catch (Exception e) {
            logger.error(format("Failed to batch commit %s %s %s", documents.size(), name(), batchId));
            logger.info(format("Resuming %s", indexConfig.getResumeIndividually()));
            if (indexConfig.getResumeIndividually()) {
                logger.debug("Resolve stacktrace", e);
                logger.info("Resuming individually");
                documents.stream().forEach(this::index);
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
                logger.info(format("Saved %s with id %s", name(), document.getId()));
                logger.info(format("Commit individual %s %s of batch %s %s", document.getId(), name(), batchId, individualCounter));
            }
        } catch (Exception e) {
            logger.warn(format("Failed to commit individual %s %s of batch %s %s", document.getId(), name(), batchId, individualCounter));
        }
    }

    @Override
    public void optimize() {
        try {
            solrClient.optimize(COLLECTION);
        } catch (Exception e) {
            logger.warn(format("Failed to optimize index"), e);
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
