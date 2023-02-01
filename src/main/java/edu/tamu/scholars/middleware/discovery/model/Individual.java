package edu.tamu.scholars.middleware.discovery.model;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.beans.Field;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(NON_EMPTY)
public class Individual extends AbstractIndexDocument {

    @Field("*")
    private Map<String, List<String>> content;

    public Individual() {

    }

    public Map<String, List<String>> getContent() {
        return content;
    }

    public void setContent(Map<String, List<String>> content) {
        this.content = content;
    }

}
