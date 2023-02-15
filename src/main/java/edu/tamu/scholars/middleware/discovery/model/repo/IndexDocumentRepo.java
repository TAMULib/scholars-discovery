package edu.tamu.scholars.middleware.discovery.model.repo;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

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
import edu.tamu.scholars.middleware.discovery.response.DiscoveryFacetAndHighlightPage;
import edu.tamu.scholars.middleware.discovery.response.DiscoveryNetwork;

public interface IndexDocumentRepo<D extends AbstractIndexDocument> {

    public <S extends D> S save(S document);

    public <S extends D> Iterable<S> saveAll(Iterable<S> documents);

    public void delete(D document);

    public void deleteById(String id);

    public void deleteAllById(Iterable<String> ids);

    public void deleteAll(Iterable<? extends D> documents);

    public void deleteAll();

    public boolean existsById(String id);

    public long count();

    public long count(String query, List<FilterArg> filters);

    public Optional<D> findById(String id);

    public List<D> findAll();

    public List<D> findAll(Sort sort);

    public Page<D> findAll(Pageable pageable);

    public List<D> findAllById(Iterable<String> ids);

    public List<D> findByType(String type);

    public List<D> findByIdIn(List<String> ids);

    public List<D> findMostRecentlyUpdate(Integer limit, List<FilterArg> filters);

    public DiscoveryFacetAndHighlightPage<D> search(QueryArg query, List<FacetArg> facets, List<FilterArg> filters, List<BoostArg> boosts, HighlightArg highlight, Pageable page);

    public CompletableFuture<Iterator<D>> export(QueryArg query, List<FilterArg> filters, List<BoostArg> boosts, Sort sort);

    public DiscoveryNetwork network(DiscoveryNetworkDescriptor dataNetworkDescriptor);

}
