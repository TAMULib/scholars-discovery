package edu.tamu.scholars.middleware.discovery.response.internal;

import org.springframework.data.domain.Page;

public interface FacetPage<T> extends FacetQueryResult<T>, Page<T> {

}
