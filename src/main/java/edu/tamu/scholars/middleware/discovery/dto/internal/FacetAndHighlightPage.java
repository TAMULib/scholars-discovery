package edu.tamu.scholars.middleware.discovery.dto.internal;

import org.springframework.data.domain.Page;

public interface FacetAndHighlightPage<T> extends HighlightQueryResult<T>, FacetQueryResult<T>, Page<T> {

}
