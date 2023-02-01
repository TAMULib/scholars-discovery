package edu.tamu.scholars.middleware.discovery.dto.internal;

import org.springframework.data.domain.Page;

public interface FacetPage<T> extends FacetQueryResult<T>, Page<T> {

}
