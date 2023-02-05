package edu.tamu.scholars.middleware.discovery.model;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import java.util.List;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.Transient;

import org.apache.solr.client.solrj.beans.Field;

import com.fasterxml.jackson.annotation.JsonInclude;

@Entity
@JsonInclude(NON_EMPTY)
public class Individual extends AbstractIndexDocument {

    @Field("*")
    @Transient
    private Map<String, List<Object>> content;

    public Individual() {

    }

    public Map<String, List<Object>> getContent() {
        return content;
    }

    public void setContent(Map<String, List<Object>> content) {
        this.content = content;
    }

}
