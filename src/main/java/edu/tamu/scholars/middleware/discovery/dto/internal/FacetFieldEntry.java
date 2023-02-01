package edu.tamu.scholars.middleware.discovery.dto.internal;

public interface FacetFieldEntry extends FacetEntry {

	@Override
	Field getKey();

	Field getField();

}
