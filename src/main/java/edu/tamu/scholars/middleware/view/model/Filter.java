package edu.tamu.scholars.middleware.view.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import edu.tamu.scholars.middleware.model.OpKey;

/**
 * Embeddable class `Filter` to describe filtering of {@link View} result sets.
 */
@Embeddable
public class Filter {

    @Column(nullable = false)
    private String field;

    @Column(nullable = false)
    private String value;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private OpKey opKey;

    public Filter() {
        opKey = OpKey.EQUALS;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public OpKey getOpKey() {
        return opKey;
    }

    public void setOpKey(OpKey opKey) {
        this.opKey = opKey;
    }

}
