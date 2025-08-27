package edu.tamu.scholars.middleware.view.model;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * A persistent representation of how a UI should render a directory view and its result set.
 * 
 * <p>See `src/main/resources/defaults/directoryView.yml`</p>
 */
@Entity
@Table(name = "directory_views")
//@ValidIndexField(message = "{DirectoryView.validIndexField}")
public class DirectoryView extends CollectionView {

    private static final long serialVersionUID = -3039416050754710347L;

    @Embedded
    private Index index;

    public DirectoryView() {
        super();
    }

    public Index getIndex() {
        return index;
    }

    public void setIndex(Index index) {
        this.index = index;
    }

}
