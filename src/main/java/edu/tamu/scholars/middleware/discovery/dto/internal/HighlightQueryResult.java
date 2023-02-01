package edu.tamu.scholars.middleware.discovery.dto.internal;

import java.util.List;

import edu.tamu.scholars.middleware.discovery.dto.internal.HighlightEntry.Highlight;

public interface HighlightQueryResult<T> {

	List<HighlightEntry<T>> getHighlighted();

	List<Highlight> getHighlights(T entity);

}
