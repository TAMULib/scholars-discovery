package edu.tamu.scholars.middleware.discovery.model.repo.custom;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import edu.tamu.scholars.middleware.discovery.argument.BoostArg;
import edu.tamu.scholars.middleware.discovery.argument.DiscoveryNetworkDescriptor;
import edu.tamu.scholars.middleware.discovery.argument.FacetArg;
import edu.tamu.scholars.middleware.discovery.argument.FilterArg;
import edu.tamu.scholars.middleware.discovery.argument.HighlightArg;
import edu.tamu.scholars.middleware.discovery.argument.QueryArg;
import edu.tamu.scholars.middleware.discovery.model.AbstractIndexDocument;
import edu.tamu.scholars.middleware.discovery.response.DiscoveryNetwork;
import edu.tamu.scholars.middleware.discovery.response.DiscoveryFacetAndHighlightPage;
import edu.tamu.scholars.middleware.shared.Cursor;

public interface IndexDocumentRepoCustom<D extends AbstractIndexDocument> {

    public long count(String query, List<FilterArg> filters);

    public List<D> findAll(List<FilterArg> filters);

    public List<D> findAll(List<FilterArg> filters, Sort sort);

    public Page<D> findAll(List<FilterArg> filters, Pageable page);

    public List<D> findByType(String type, List<FilterArg> filters);

    public List<D> findMostRecentlyUpdate(Integer limit);

    public List<D> findMostRecentlyUpdate(Integer limit, List<FilterArg> filters);

    public DiscoveryFacetAndHighlightPage<D> search(QueryArg query, List<FacetArg> facets, List<FilterArg> filters, List<BoostArg> boosts, HighlightArg highlight, Pageable page);

    public Cursor<D> stream(QueryArg query, List<FilterArg> filters, List<BoostArg> boosts, Sort sort);

    public DiscoveryNetwork getDiscoveryNetwork(DiscoveryNetworkDescriptor dataNetworkDescriptor);

}
