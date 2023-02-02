package edu.tamu.scholars.middleware.discovery.response.internal;

import org.springframework.data.domain.Page;

public interface FacetQueryResult<T> {

    Page<FacetFieldEntry> getFacetResultPage(String fieldname);

    Page<FacetFieldEntry> getRangeFacetResultPage(String fieldname);

}
