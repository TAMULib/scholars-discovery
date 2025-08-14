package edu.tamu.scholars.middleware.discovery.model;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;
import static edu.tamu.scholars.middleware.discovery.DiscoveryConstants.ABSTRACT;
import static edu.tamu.scholars.middleware.discovery.DiscoveryConstants.ABSTRACT_TEXT;
import static edu.tamu.scholars.middleware.discovery.DiscoveryConstants.CLASS;
import static edu.tamu.scholars.middleware.discovery.DiscoveryConstants.ID;
import static edu.tamu.scholars.middleware.discovery.DiscoveryConstants.SYNC_IDS;
import static edu.tamu.scholars.middleware.discovery.DiscoveryConstants.TYPE;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.apache.lucene.document.StoredField;
import org.apache.solr.client.solrj.beans.Field;
import org.apache.solr.common.SolrDocument;
import org.springframework.hateoas.server.core.Relation;

import edu.tamu.scholars.middleware.discovery.utility.DiscoveryUtility;

/**
 * 
 */
@JsonInclude(NON_EMPTY)
@Relation(collectionRelation = "individual", itemRelation = "individual")
public class Individual extends AbstractIndexDocument {

    @Field("*")
    private final Map<String, Object> content;

    public Individual() {
        this(new HashMap<>());
    }

    private Individual(Map<String, Object> content) {
        this.content = content;
    }

    public Map<String, Object> getContent() {
        return content;
    }

    public String getAbstract() {
        return (String) content.get(ABSTRACT_TEXT);
    }

    @Override
    public String getProxy() {
        return (String) content.get(CLASS);
    }

    @Override
    public void setProxy(String proxy) {
        this.content.put(CLASS, proxy);
    }

    @Override
    public String getId() {
        return (String) content.get(ID);
    }

    @Override
    public void setId(String id) {
        this.content.put(ID, id);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<String> getType() {
        return (List<String>) content.get(TYPE);
    }

    @Override
    public void setType(List<String> type) {
        this.content.put(TYPE, type);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<String> getSyncIds() {
        return (List<String>) content.get(SYNC_IDS);
    }

    @Override
    public void setSyncIds(List<String> syncIds) {
        this.content.put(SYNC_IDS, syncIds);
    }

    public static Object normalize(Object value) {
        if (StoredField.class.isAssignableFrom(value.getClass())) {
            return ((StoredField) value).stringValue();
        }
        return value;
    }

    public static Individual from(SolrDocument document) {
        Map<String, Object> content = new HashMap<>();

        String name = (String) normalize(document.getFieldValue(CLASS));

        DiscoveryUtility.getDiscoveryDocumentTypeFields(name)
            .entrySet()
            .stream()
            .filter(entry -> document.containsKey(entry.getKey()))
            .forEach(entry -> {
                String field = entry.getKey();
                if (Collection.class.isAssignableFrom(entry.getValue())) {
                    Collection<Object> values = document.getFieldValues(field)
                        .stream()
                        .map(Individual::normalize)
                        .collect(Collectors.toList());
                    content.put(field, values);
                } else {
                    content.put(field, normalize(document.getFirstValue(field)));
                }
            });

        if (document.containsKey(ABSTRACT_TEXT)) {
            content.put(ABSTRACT, normalize(document.getFirstValue(ABSTRACT_TEXT)));
        }

        return Individual.from(content);
    }

    public static Individual from(Map<String, Object> content) {
        return new Individual(content);
    }

    @Override
    public String toString() {
        return "Individual [content=" + content + "]";
    }

}
