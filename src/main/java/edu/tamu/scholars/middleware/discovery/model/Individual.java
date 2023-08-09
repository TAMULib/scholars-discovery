package edu.tamu.scholars.middleware.discovery.model;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;
import static edu.tamu.scholars.middleware.discovery.DiscoveryConstants.CLASS;
import static edu.tamu.scholars.middleware.discovery.DiscoveryConstants.ID;
import static edu.tamu.scholars.middleware.discovery.DiscoveryConstants.MOD_TIME;
import static edu.tamu.scholars.middleware.discovery.DiscoveryConstants.SYNC_IDS;
import static edu.tamu.scholars.middleware.discovery.DiscoveryConstants.TYPE;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.solr.client.solrj.beans.Field;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.hateoas.server.core.Relation;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(NON_EMPTY)
@Relation(collectionRelation = "individual", itemRelation = "individual")
public class Individual extends AbstractIndexDocument {

    @Field("*")
    private Map<String, Collection<Object>> content;

    public Individual() {
        this.content = new HashMap<>();
    }

    public Map<String, Collection<Object>> getContent() {
        return content;
    }

    public void setContent(Map<String, Collection<Object>> content) {
        this.content = content;
    }

    public static List<Individual> fromSolrDocumentList(SolrDocumentList documents) {
        return documents.parallelStream()
            .map(Individual::fromSolrDocument)
            .collect(Collectors.toList());
    }

    public static Individual fromSolrDocument(SolrDocument document) {
        Individual individual = new Individual();

        individual.setId(document.getFieldValue(ID).toString());
        individual.setClazz(document.getFieldValue(CLASS).toString());
        if (Objects.nonNull(document.getFieldValues(TYPE))) {
            individual.setType(document.getFieldValues(TYPE).stream().map(to -> to.toString()).collect(Collectors.toList()));
        }

        if (Objects.nonNull(document.getFieldValues(SYNC_IDS))) {
            individual.setSyncIds(document.getFieldValues(SYNC_IDS).stream().map(to -> to.toString()).collect(Collectors.toList()));
        }

        individual.setModTime(document.getFieldValue(MOD_TIME).toString());

        individual.setContent(document.getFieldValuesMap());

        return individual;
    }

    public static List<SolrInputDocument> toSolrInputDocuments(Collection<Individual> individuals) {
        return individuals.parallelStream()
            .map(Individual::toSolrInputDocument)
            .collect(Collectors.toList());
    }

    public static SolrInputDocument toSolrInputDocument(Individual individual) {
        SolrInputDocument document = new SolrInputDocument();

        individual.getContent().entrySet().parallelStream().forEach(entry -> {
            document.addField(entry.getKey(), entry.getValue());
        });

        return document;
    }

}
