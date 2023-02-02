package edu.tamu.scholars.middleware.discovery.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.ElementCollection;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import org.apache.solr.client.solrj.beans.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

import edu.tamu.scholars.middleware.discovery.annotation.PropertySource;
import edu.tamu.scholars.middleware.discovery.annotation.PropertyTarget;

@MappedSuperclass
public abstract class AbstractIndexDocument {

    @Id
    @Field
    @PropertyTarget(required = true, readonly = true)
    private String id;

    @Transient
    @Field
    @PropertyTarget(type = "whole_strings")
    @PropertySource(template = "common/type", predicate = "http://vitro.mannlib.cornell.edu/ns/vitro/0.7#mostSpecificType", parse = true)
    private List<String> type;

    @Transient
    @Field("class")
    @JsonProperty("class")
    @PropertyTarget(type = "string", value = "class", required = true)
    private String clazz = this.getClass().getSimpleName();

    @Field
    @ElementCollection
    @PropertyTarget(type = "strings")
    private List<String> syncIds = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getType() {
        return type;
    }

    public void setType(List<String> type) {
        this.type = type;
    }

    public String getClazz() {
        return clazz;
    }

    public void setClazz(String clazz) {
        this.clazz = clazz;
    }

    public List<String> getSyncIds() {
        return syncIds;
    }

    public void setSyncIds(List<String> syncIds) {
        this.syncIds = syncIds;
    }

}
