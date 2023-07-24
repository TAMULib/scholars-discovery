package edu.tamu.scholars.middleware.view.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

@Entity
@Table(name = "analytic_views")
public class AnalyticView extends CollectionView {

    private static final long serialVersionUID = 2912876591264398726L;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ContainerType type;

    public AnalyticView() {
        super();
    }

    public ContainerType getType() {
        return type;
    }

    public void setGraph(ContainerType type) {
        this.type = type;
    }

}
