package edu.tamu.scholars.middleware.view.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

/**
 * A persistent representation of how a UI should render a data and analytics view.
 * 
 * <p>See `src/main/resources/defaults/dataAndAnalyticViews.yml`</p>
 */
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
