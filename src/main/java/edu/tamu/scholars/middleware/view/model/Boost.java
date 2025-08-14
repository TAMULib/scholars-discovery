package edu.tamu.scholars.middleware.view.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * Embeddable domain model for {@link CollectionView} result set term relevancy boosting.
 */
@Embeddable
public class Boost {

    @Column(nullable = false)
    private String field;

    @Column(nullable = false)
    private float value;

    public Boost() {

    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }

}
