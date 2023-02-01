package edu.tamu.scholars.middleware.discovery.dto.internal;

import org.springframework.data.domain.Page;

public interface FacetQueryResult<T> {

	Page<FacetFieldEntry> getFacetResultPage(String fieldname);

	Page<FacetFieldEntry> getRangeFacetResultPage(String fieldname);

}
