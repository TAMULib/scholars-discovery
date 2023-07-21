package edu.tamu.scholars.middleware.view.model;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "analytic_views")
public class AnalyticView extends CollectionView {

    private static final long serialVersionUID = 2912876591264398726L;

    public AnalyticView() {
        super();
    }

}
