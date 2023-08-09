package edu.tamu.scholars.middleware.discovery.model;

import static edu.tamu.scholars.middleware.discovery.DiscoveryConstants.CLASS;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import edu.tamu.scholars.middleware.discovery.annotation.FieldSource;
import edu.tamu.scholars.middleware.discovery.annotation.FieldType;

public abstract class AbstractIndexDocument {

    @FieldType(required = true, readonly = true)
    private String id;

    @JsonProperty(CLASS)
    private String clazz = this.getClass().getSimpleName();

    @FieldType(type = "whole_strings")
    @FieldSource(template = "common/type", predicate = "http://vitro.mannlib.cornell.edu/ns/vitro/0.7#mostSpecificType", parse = true)
    private List<String> type;

    @FieldType(type = "strings")
    private List<String> syncIds = new ArrayList<>();

    @FieldType(type = "pdate")
    @FieldSource(template = "common/modTime", predicate = "http://vitro.mannlib.cornell.edu/ns/vitro/0.7#modTime")
    private String modTime;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getClazz() {
        return clazz;
    }

    public void setClazz(String clazz) {
        this.clazz = clazz;
    }

    public List<String> getType() {
        return type;
    }

    public void setType(List<String> type) {
        this.type = type;
    }

    public List<String> getSyncIds() {
        return syncIds;
    }

    public void setSyncIds(List<String> syncIds) {
        this.syncIds = syncIds;
    }

    public String getModTime() {
        return modTime;
    }

    public void setModTime(String modTime) {
        this.modTime = modTime;
    }

}
