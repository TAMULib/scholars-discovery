package edu.tamu.scholars.middleware.view.model;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * A persistent representation of how a UI should render a discovery view and its result set.
 * 
 * <p>See `src/main/resources/defaults/discoveryViews.yml`</p>
 */
@Entity
@JsonInclude(NON_EMPTY)
@Table(name = "discovery_views")
public class DiscoveryView extends CollectionView {

    private static final long serialVersionUID = 6627502439871091387L;

    @Column(nullable = true)
    private String defaultSearchField;

    @ElementCollection
    private List<String> highlightFields;

    @Column(nullable = true)
    private String highlightPrefix;

    @Column(nullable = true)
    private String highlightPostfix;

    public DiscoveryView() {
        super();
        this.highlightFields = new ArrayList<>();
    }

    public String getDefaultSearchField() {
        return defaultSearchField;
    }

    public void setDefaultSearchField(String defaultSearchField) {
        this.defaultSearchField = defaultSearchField;
    }

    public List<String> getHighlightFields() {
        return highlightFields;
    }

    public void setHighlightFields(List<String> highlightFields) {
        this.highlightFields = highlightFields;
    }

    public String getHighlightPrefix() {
        return highlightPrefix;
    }

    public void setHighlightPre(String highlightPrefix) {
        this.highlightPrefix = highlightPrefix;
    }

    public String getHighlightPostfix() {
        return highlightPostfix;
    }

    public void setHighlightPost(String highlightPostfix) {
        this.highlightPostfix = highlightPostfix;
    }

}
