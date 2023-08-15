package edu.tamu.scholars.middleware.discovery.component.solr;

import static edu.tamu.scholars.middleware.discovery.service.IndexService.CREATED_FIELDS;

import java.lang.reflect.Field;
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
import org.springframework.beans.factory.annotation.Value;

import edu.tamu.scholars.middleware.discovery.annotation.FieldType;
import edu.tamu.scholars.middleware.discovery.component.Indexer;
import edu.tamu.scholars.middleware.discovery.model.AbstractIndexDocument;
import edu.tamu.scholars.middleware.discovery.model.Individual;

public class SolrIndexer implements Indexer {

    private static final Logger logger = LoggerFactory.getLogger(SolrIndexer.class);

    @Autowired
    private SolrClient solrClient;

    @Value("${middleware.index.name}")
    private String collectionName;

    @Value("${middleware.index.enableIndividualOnBatchFail:false}")
    private boolean enableIndividualOnBatchFail;

    private final Class<AbstractIndexDocument> type;

    public SolrIndexer(Class<AbstractIndexDocument> type) {
        this.type = type;
    }

    @Override
    public void init() {
        for (Field field : FieldUtils.getFieldsListWithAnnotation(type, FieldType.class)) {
            FieldType fieldType = field.getAnnotation(FieldType.class);

            String name = StringUtils.isNotEmpty(fieldType.value())
                ? fieldType.value()
                : field.getName();

            if (!fieldType.readonly() && !CREATED_FIELDS.contains(name) && CREATED_FIELDS.add(name)) {
                Map<String, Object> fieldAttributes = new HashMap<String,Object>();

                fieldAttributes.put("type", fieldType.type());
                fieldAttributes.put("stored", fieldType.stored());
                fieldAttributes.put("indexed", fieldType.searchable());
                fieldAttributes.put("required", fieldType.required());

                if (StringUtils.isNotEmpty(fieldType.defaultValue())) {
                    fieldAttributes.put("defaultValue", fieldType.defaultValue());
                }

                fieldAttributes.put("multiValued", Collection.class.isAssignableFrom(field.getType()));

                fieldAttributes.put("name", name);

                try {
                    SchemaRequest.AddField addFieldRequest = new SchemaRequest.AddField(fieldAttributes);
                    addFieldRequest.process(solrClient, collectionName);

                    if (fieldType.copyTo().length > 0) {
                        try {
                            SchemaRequest.AddCopyField addCopyFieldRequest = new SchemaRequest.AddCopyField(name, Arrays.asList(fieldType.copyTo()));
                            addCopyFieldRequest.process(solrClient, collectionName);
                        } catch (Exception e) {
                            logger.error("Failed to add copy field", e);
                        }
                    }
                } catch (Exception e) {
                    logger.debug("Failed to add field", e);
                }
            }
        }
    }

    @Override
    public void index(Collection<Individual> individuals) {
        try {
            solrClient.addBeans(collectionName, individuals);
            solrClient.commit(collectionName);
            logger.info("Saved {} batch of {}", name(), individuals.size());
        } catch (Exception e) {
            logger.debug("Error saving batch", e);
            if (enableIndividualOnBatchFail) {
                logger.warn("Failed to save batch of {}. Attempting individually.", name());
                individuals.stream().forEach(this::index);
            } else {
                logger.warn("Skipping individuals of failed batch of {}. {}", name(), e.getMessage());
            }
        }
    }

    @Override
    public void index(Individual individual) {
        try {
            solrClient.addBean(collectionName, individual);
            solrClient.commit(collectionName);
            logger.info("Saved {} with id {}", name(), individual.getId());
        } catch (Exception e) {
            logger.debug("Error saving individual", e);
            logger.warn("Failed to save {} with id {}", name(), individual.getId());
        }
    }

    @Override
    public void optimize() {
        try {
            solrClient.optimize(collectionName);
        } catch (Exception e) {
            logger.debug("Error optimizing collection", e);
            logger.warn("Failed to optimize collection. {}", e.getMessage());
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
