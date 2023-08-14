package edu.tamu.scholars.middleware.view.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

@Entity
@Table(name = "data_and_analytics_views")
public class DataAndAnalyticsView extends CollectionView {

    private static final long serialVersionUID = 2912876591264398726L;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ContainerType type;

    public ContainerType getType() {
        return type;
    }

    public void setType(ContainerType type) {
        this.type = type;
    }

}
