package edu.tamu.scholars.middleware.view.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import edu.tamu.scholars.middleware.model.FilterOp;

@Embeddable
public class Filter {

    @Column(nullable = false)
    private String field;

    @Column(nullable = false)
    private String value;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private FilterOp opKey;

    public Filter() {
        opKey = FilterOp.EQUALS;
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

    public FilterOp getOpKey() {
        return opKey;
    }

    public void setOpKey(FilterOp opKey) {
        this.opKey = opKey;
    }

}
