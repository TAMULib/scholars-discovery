package edu.tamu.scholars.middleware.view.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import edu.tamu.scholars.middleware.model.GroupOp;

@Embeddable
public class FilterGroup {

    @Column(nullable = false)
    private String a;

    @Column(nullable = false)
    private String b;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private GroupOp opKey;

    public FilterGroup() {
        opKey = GroupOp.OR;
    }

    public String getA() {
        return a;
    }

    public void setA(String a) {
        this.a = a;
    }

    public String getB() {
        return b;
    }

    public void setB(String b) {
        this.b = b;
    }

    public GroupOp getOpKey() {
        return opKey;
    }

    public void setOpKey(GroupOp opKey) {
        this.opKey = opKey;
    }

}
