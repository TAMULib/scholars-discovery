package edu.tamu.scholars.middleware.discovery.response.internal;

public interface FacetFieldEntry extends FacetEntry {

    @Override
    Field getKey();

    Field getField();

}
