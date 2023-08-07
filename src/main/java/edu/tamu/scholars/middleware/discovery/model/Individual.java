package edu.tamu.scholars.middleware.discovery.model;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;
import static edu.tamu.scholars.middleware.discovery.DiscoveryConstants.CLASS;
import static edu.tamu.scholars.middleware.discovery.DiscoveryConstants.ID;
import static edu.tamu.scholars.middleware.discovery.DiscoveryConstants.SYNC_IDS;
import static edu.tamu.scholars.middleware.discovery.DiscoveryConstants.TYPE;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.solr.common.SolrDocument;
import org.springframework.hateoas.server.core.Relation;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(NON_EMPTY)
@Relation(collectionRelation = "individual", itemRelation = "individual")
public class Individual extends AbstractIndexDocument {

    private Map<String, Collection<Object>> content;

    public Individual() {

    }

    public Map<String, Collection<Object>> getContent() {
        return content;
    }

    public void setContent(Map<String, Collection<Object>> content) {
        this.content = content;
    }

    public static Individual from(SolrDocument document) {
        Individual individual = new Individual();

        System.out.println("\n\n" + document + "\n\n");

        individual.setId((String) document.get(ID));
        individual.setClazz((String) document.get(CLASS));
        individual.setContent(document.getFieldValuesMap());
        individual.setType((List<String>) document.get(TYPE));
        individual.setSyncIds((List<String>) document.get(SYNC_IDS));

        return individual;
    }

}
