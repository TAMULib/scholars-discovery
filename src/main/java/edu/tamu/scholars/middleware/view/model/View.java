package edu.tamu.scholars.middleware.view.model;

import jakarta.persistence.MappedSuperclass;

import edu.tamu.scholars.middleware.model.Named;

/**
 * Abstract mapped superclass `View` to differentiate between persistent views.
 */
@MappedSuperclass
public abstract class View extends Named {

    private static final long serialVersionUID = 413593021970972190L;

    public View() {
        super();
    }

}
