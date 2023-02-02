package edu.tamu.scholars.middleware.discovery.model.repo.impl;

import static edu.tamu.scholars.middleware.discovery.DiscoveryConstants.CORE_NAME;
import static edu.tamu.scholars.middleware.discovery.DiscoveryConstants.DEFAULT_QUERY;
import static edu.tamu.scholars.middleware.discovery.DiscoveryConstants.ID;
import static edu.tamu.scholars.middleware.discovery.DiscoveryConstants.QUERY_TEMPLATE;
import static edu.tamu.scholars.middleware.discovery.DiscoveryConstants.TYPE;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.collections4.IterableUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.SolrParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery;

import edu.tamu.scholars.middleware.discovery.argument.BoostArg;
import edu.tamu.scholars.middleware.discovery.argument.DataNetworkDescriptor;
import edu.tamu.scholars.middleware.discovery.argument.FacetArg;
import edu.tamu.scholars.middleware.discovery.argument.FilterArg;
import edu.tamu.scholars.middleware.discovery.argument.HighlightArg;
import edu.tamu.scholars.middleware.discovery.argument.QueryArg;
import edu.tamu.scholars.middleware.discovery.model.Individual;
import edu.tamu.scholars.middleware.discovery.model.repo.SolrDocumentRepo;
import edu.tamu.scholars.middleware.discovery.response.DataNetwork;
import edu.tamu.scholars.middleware.discovery.response.internal.FacetAndHighlightPage;
import edu.tamu.scholars.middleware.model.OpKey;
import edu.tamu.scholars.middleware.shared.Cursor;

public class IndividualRepoImpl implements SolrDocumentRepo<Individual> {

    private static final Logger logger = LoggerFactory.getLogger(IndividualRepoImpl.class);

    private static final Pattern RANGE_PATTERN = Pattern.compile("^\\[(.*?) TO (.*?)\\]$");

    @Value("${spring.data.solr.parser:edismax}")
    private String defType;

    @Value("${spring.data.solr.operator:AND}")
    private String defaultOperator;

    @Autowired
    private SolrClient solrClient;

    @Override
    public DataNetwork getDataNetwork(DataNetworkDescriptor dataNetworkDescriptor) {
        final String id = dataNetworkDescriptor.getId();
        final DataNetwork dataNetwork = DataNetwork.to(id);

        try {
            final SolrParams queryParams = dataNetworkDescriptor.getSolrParams();

            final QueryResponse response = solrClient.query(CORE_NAME, queryParams);

            final SolrDocumentList documents = response.getResults();

            final String dateField = dataNetworkDescriptor.getDateField();

            for (SolrDocument document : documents) {
                if (document.containsKey(dateField)) {
                    Date publicationDate = ((Date) document.getFieldValue(dateField));
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(publicationDate);
                    dataNetwork.countYear(String.valueOf(calendar.get(Calendar.YEAR)));
                }
                List<String> values = getValues(document, dataNetworkDescriptor.getDataFields());

                String iid = (String) document.getFieldValue(ID);

                for (String v1 : values) {
                    dataNetwork.index(v1);

                    if (!v1.endsWith(id)) {
                        dataNetwork.countLink(v1);
                    }
                    for (String v2 : values) {
                        // prefer id as source
                        if (v2.endsWith(id)) {
                            dataNetwork.map(iid, v2, v1);
                        } else {
                            dataNetwork.map(iid, v1, v2);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Failed to build data network!", e);
        }

        return dataNetwork;
    }

    @Override
    public long count(String query, List<FilterArg> filters) {
        return 0;
    }

    @Override
    public List<Individual> findAll(List<FilterArg> filters) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Individual> findAll(List<FilterArg> filters, Sort sort) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Page<Individual> findAll(List<FilterArg> filters, Pageable page) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Individual> findMostRecentlyUpdate(Integer limit) {
        return findMostRecentlyUpdate(limit, new ArrayList<FilterArg>());
    }

    @Override
    public List<Individual> findByType(String type, List<FilterArg> filters) {
        filters.add(FilterArg.of(TYPE, Optional.of(type), Optional.of(OpKey.EQUALS.getKey()), Optional.empty()));
        return findAll(filters);
    }

    @Override
    public List<Individual> findMostRecentlyUpdate(Integer limit, List<FilterArg> filters) {
        throw new UnsupportedOperationException();
    }

    @Override
    public FacetAndHighlightPage<Individual> search(QueryArg query, List<FacetArg> facets, List<FilterArg> filters, List<BoostArg> boosts, HighlightArg highlight, Pageable page) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Cursor<Individual> stream(QueryArg query, List<FilterArg> filters, List<BoostArg> boosts, Sort sort) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <S extends Individual> S save(S document) {
        return save(document, 250);
    }

    @Override
    public void delete(Individual document) {
        deleteById(document.getId());
    }

    @Override
    public List<Individual> findByType(String type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Individual> findByIdIn(List<String> ids) {
        return findAllById(ids);
    }

    @Override
    public List<Individual> findBySyncIds(String syncId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Individual> findBySyncIdsIn(List<String> syncIds) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <S extends Individual> S save(S entity, int commitWithinMs) {
        try {
            solrClient.addBean(CORE_NAME, entity, commitWithinMs);
        } catch (IOException | SolrServerException e) {
            throw new RuntimeException(e);
        }
        return entity;
    }

    @Override
    public <S extends Individual> Iterable<S> saveAll(Iterable<S> entities, int commitWithinMs) {
        List<S> individuals = IterableUtils.toList(entities);
        try {
            solrClient.addBeans(CORE_NAME, individuals, commitWithinMs);
        } catch (IOException | SolrServerException e) {
            throw new RuntimeException(e);
        }
        return entities;
    }

    @Override
    public long count() {
        return count(DEFAULT_QUERY);
    }

    @Override
    public List<Individual> findAll() {
        try {
            SolrQuery query = new SolrQuery(DEFAULT_QUERY)
                .setRows(Integer.MAX_VALUE);

            return solrClient.query(CORE_NAME, query)
                .getBeans(Individual.class);
        } catch (IOException | SolrServerException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Individual> findAll(Sort sort) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Individual> findAllById(Iterable<String> ids) {
        try {
            SolrDocumentList documents = solrClient.getById(CORE_NAME, IterableUtils.toList(ids));

            return solrClient.getBinder()
                .getBeans(Individual.class, documents);
        } catch (IOException | SolrServerException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <S extends Individual> List<S> saveAll(Iterable<S> entities) {
        return IterableUtils.toList(saveAll(entities, 250));
    }

    @Override
    public Individual getOne(String id) {
        return getById(id);
    }

    @Override
    public Individual getById(String id) {
        try {
            SolrDocument document = solrClient.getById(CORE_NAME, id);

            return solrClient.getBinder()
                .getBean(Individual.class, document);
        } catch (IOException | SolrServerException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Page<Individual> findAll(Pageable pageable) {
        try {
            SolrQuery query = new SolrQuery(DEFAULT_QUERY)
                .setRows(pageable.getPageSize())
                .setStart((int) pageable.getOffset());
            // TODO: apply sorting
            SolrDocumentList documents = solrClient.query(CORE_NAME, query)
                .getResults();
            List<Individual> individuals = solrClient.getBinder()
                .getBeans(Individual.class, documents);

            return new PageImpl<Individual>(individuals, pageable, documents.getNumFound());
        } catch (IOException | SolrServerException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Individual> findById(String id) {
        return Optional.ofNullable(getById(id));
    }

    @Override
    public boolean existsById(String id) {
        return count(String.format(QUERY_TEMPLATE, ID, id)) == 1;
    }

    @Override
    public void deleteById(String id) {
        try {
            solrClient.deleteById(CORE_NAME, id, 250);
        } catch (IOException | SolrServerException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void deleteAllById(Iterable<? extends String> ids) {
        try {
            solrClient.deleteById(CORE_NAME, IterableUtils.toList((Iterable<String>) ids), 250);
        } catch (IOException | SolrServerException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteAll(Iterable<? extends Individual> entities) {
        List<String> ids = IterableUtils.toList(entities).stream()
            .map(i -> i.getId())
            .collect(Collectors.toList());
        deleteAllById(ids);
    }

    @Override
    public void deleteAll() {
        try {
            solrClient.deleteByQuery(CORE_NAME, DEFAULT_QUERY, 250);
        } catch (IOException | SolrServerException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void flush() {
        throw new UnsupportedOperationException();
    }

    @Override
    public <S extends Individual> S saveAndFlush(S entity) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <S extends Individual> List<S> saveAllAndFlush(Iterable<S> entities) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteAllInBatch(Iterable<Individual> entities) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteAllByIdInBatch(Iterable<String> ids) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteAllInBatch() {
        throw new UnsupportedOperationException();
    }

    @Override
    public <S extends Individual> Optional<S> findOne(Example<S> example) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <S extends Individual> List<S> findAll(Example<S> example) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <S extends Individual> List<S> findAll(Example<S> example, Sort sort) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <S extends Individual> Page<S> findAll(Example<S> example, Pageable pageable) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <S extends Individual> long count(Example<S> example) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <S extends Individual> boolean exists(Example<S> example) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <S extends Individual, R> R findBy(Example<S> example, Function<FetchableFluentQuery<S>, R> queryFunction) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Class<Individual> type() {
        return Individual.class;
    }

    private long count(String q) {
        SolrQuery query = new SolrQuery(q)
            .setRows(0);
        return count(query);
    }

    private long count(SolrQuery query) {
        try {
            return solrClient.query(CORE_NAME, query)
                .getResults()
                .getNumFound();
        } catch (IOException | SolrServerException e) {
            throw new RuntimeException(e);
        }
    }

    private List<String> getValues(SolrDocument document, List<String> dataFields) {
        return dataFields.stream()
            .filter(v -> document.containsKey(v))
            .flatMap(v -> document.getFieldValues(v).stream())
            .map(v -> (String) v)
            .collect(Collectors.toList());
    }

}
