package edu.tamu.scholars.middleware.discovery.model.repo.impl;

import static edu.tamu.scholars.middleware.discovery.DiscoveryConstants.CLASS;
import static edu.tamu.scholars.middleware.discovery.DiscoveryConstants.CORE_NAME;
import static edu.tamu.scholars.middleware.discovery.DiscoveryConstants.DEFAULT_QUERY;
import static edu.tamu.scholars.middleware.discovery.DiscoveryConstants.ID;
import static edu.tamu.scholars.middleware.discovery.DiscoveryConstants.QUERY_DELIMETER;
import static edu.tamu.scholars.middleware.discovery.DiscoveryConstants.QUERY_TEMPLATE;
import static edu.tamu.scholars.middleware.discovery.DiscoveryConstants.REQUEST_PARAM_DELIMETER;
import static edu.tamu.scholars.middleware.discovery.DiscoveryConstants.TYPE;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.SolrParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
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
import edu.tamu.scholars.middleware.discovery.model.repo.IndexDocumentRepo;
import edu.tamu.scholars.middleware.discovery.response.DataNetwork;
import edu.tamu.scholars.middleware.discovery.response.DiscoveryFacetAndHighlightPage;
import edu.tamu.scholars.middleware.model.OpKey;
import edu.tamu.scholars.middleware.shared.Cursor;

public class IndividualRepoImpl implements IndexDocumentRepo<Individual> {

    private static final Logger logger = LoggerFactory.getLogger(IndividualRepoImpl.class);

    private static final Pattern RANGE_PATTERN = Pattern.compile("^\\[(.*?) TO (.*?)\\]$");

    @Value("${spring.data.solr.parser:edismax}")
    private String defType;

    @Value("${spring.data.solr.operator:AND}")
    private String defaultOperator;

    @Value("${spring.data.solr.commitWithinMs:250}")
    private int commitWithinMs;

    @Lazy
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
        SolrQueryBuilder builder = new SolrQueryBuilder()
            .withQuery(query)
            .withFilters(filters);

        return count(builder.query());
    }

    @Override
    public List<Individual> findAll(List<FilterArg> filters) {
        SolrQueryBuilder builder = new SolrQueryBuilder()
            .withFilters(filters);

        return findAll(builder.query());
    }

    @Override
    public List<Individual> findAll(List<FilterArg> filters, Sort sort) {
        SolrQueryBuilder builder = new SolrQueryBuilder()
            .withFilters(filters)
            .withSort(sort);

        return findAll(builder.query());
    }

    @Override
    public Page<Individual> findAll(List<FilterArg> filters, Pageable page) {
        SolrQueryBuilder builder = new SolrQueryBuilder()
            .withFilters(filters);

        return findAll(builder.query(), page);
    }

    @Override
    public List<Individual> findByType(String type, List<FilterArg> filters) {
        filters.add(FilterArg.of(TYPE, Optional.of(type), Optional.of(OpKey.EQUALS.getKey()), Optional.empty()));

        return findAll(filters);
    }

    @Override
    public List<Individual> findMostRecentlyUpdate(Integer limit) {
        return findMostRecentlyUpdate(limit, new ArrayList<FilterArg>());
    }

    @Override
    public List<Individual> findMostRecentlyUpdate(Integer limit, List<FilterArg> filters) {
        SolrQueryBuilder builder = new SolrQueryBuilder()
            .withFilters(filters)
            .withRows(limit);

        return findAll(builder.query());
    }

    @Override
    // @formatter:off
    public DiscoveryFacetAndHighlightPage<Individual> search(
        QueryArg query,
        List<FacetArg> facets,
        List<FilterArg> filters,
        List<BoostArg> boosts,
        HighlightArg highlight,
        Pageable page
    ) {
    // @formatter:on
        SolrQueryBuilder builder = new SolrQueryBuilder()
            .withQuery(query)
            .withFacets(facets)
            .withFilters(filters)
            .withBoosts(boosts)
            .withHighlight(highlight)
            .withPage(page);

        try {
            QueryResponse response = solrClient.query(CORE_NAME, builder.query());
            
            return DiscoveryFacetAndHighlightPage.from(response, page, facets, highlight, Individual.class);
            
            
        } catch (IOException | SolrServerException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Cursor<Individual> stream(QueryArg query, List<FilterArg> filters, List<BoostArg> boosts, Sort sort) {
        SolrQueryBuilder builder = new SolrQueryBuilder()
            .withQuery(query)
            .withFilters(filters)
            .withBoosts(boosts)
            .withSort(sort);

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
        SolrQueryBuilder builder = new SolrQueryBuilder()
            .withRows(Integer.MAX_VALUE);

        return findAll(builder.query());
    }

    @Override
    public List<Individual> findAll(Sort sort) {
        SolrQueryBuilder builder = new SolrQueryBuilder()
            .withRows(Integer.MAX_VALUE)
            .withSort(sort);

        return findAll(builder.query());
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
        SolrQueryBuilder builder = new SolrQueryBuilder()
            .withStart((int) pageable.getOffset())
            .withRows(pageable.getPageSize())
            .withSort(pageable.getSort());

        return findAll(builder.query(), pageable);
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

    private List<Individual> findAll(SolrQuery query) {
        try {
            return solrClient.query(CORE_NAME, query)
                    .getBeans(Individual.class);
        } catch (IOException | SolrServerException e) {
            throw new RuntimeException(e);
        }
    }

    private Page<Individual> findAll(SolrQuery query, Pageable pageable) {
        try {
            SolrDocumentList documents = solrClient.query(CORE_NAME, query)
                    .getResults();
            List<Individual> individuals = solrClient.getBinder()
                    .getBeans(Individual.class, documents);

            return new PageImpl<Individual>(individuals, pageable, documents.getNumFound());
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

    private class SolrQueryBuilder {

        private final SolrQuery query;

        private SolrQueryBuilder() {
            this.query = new SolrQuery()
                .setParam("defType", defType)
                .setParam("q.op", defaultOperator)
                .setQuery(DEFAULT_QUERY);
        }

        public SolrQueryBuilder withQuery(QueryArg query) {

            if (StringUtils.isNotEmpty(query.getDefaultField())) {
                this.query.setParam("df", query.getDefaultField());
            }

            if (StringUtils.isNotEmpty(query.getMinimumShouldMatch())) {
                this.query.setParam("mm", query.getMinimumShouldMatch());
            }

            if (StringUtils.isNotEmpty(query.getQueryField())) {
                this.query.setParam("qf", query.getQueryField());
            }

            if (StringUtils.isNotEmpty(query.getBoostQuery())) {
                this.query.setParam("bq", query.getBoostQuery());
            }

            if (StringUtils.isNotEmpty(query.getFields())) {
                String fields = String.join(REQUEST_PARAM_DELIMETER, ID, CLASS, query.getFields());
                String fl = String.join(REQUEST_PARAM_DELIMETER, Arrays.stream(fields.split(REQUEST_PARAM_DELIMETER)).collect(Collectors.toSet()));
                this.query.setParam("fl", fl);
            }

            return withQuery(query.getExpression());
        }

        public SolrQueryBuilder withQuery(String query) {
            this.query.setQuery(query);

            return this;
        }

        public SolrQueryBuilder withPage(Pageable page) {
            return withStart((int) page.getOffset())
                .withRows(page.getPageSize())
                .withSort(page.getSort());
        }

        public SolrQueryBuilder withStart(int start) {
            this.query.setStart(start);

            return this;
        }

        public SolrQueryBuilder withRows(int rows) {
            this.query.setRows(rows);

            return this;
        }

        public SolrQueryBuilder withSort(Sort sort) {
            sort.iterator().forEachRemaining(order -> {
                this.query.setSort(order.getProperty(), order.getDirection().isAscending() ? ORDER.asc: ORDER.desc);
            });

            return this;
        }

        public SolrQueryBuilder withFilters(List<FilterArg> filters) {

            filters.stream().collect(Collectors.groupingBy(w -> w.getField())).forEach((field, filterList) -> {
                FilterArg firstOne = filterList.get(0);
                StringBuilder filterQuery = new StringBuilder()
                    .append(new FilterQueryBuilder(firstOne, false).build());
                if (filterList.size() > 1) {
                    // NOTE: filters grouped by field are AND together
                    for (FilterArg arg : filterList.subList(1, filterList.size())) {
                        filterQuery.append(" AND ")
                            .append(new FilterQueryBuilder(arg, true).build());
                    }
                }
                this.query.addFilterQuery(filterQuery.toString());
            });

            return this;
        }

        public SolrQueryBuilder withFacets(List<FacetArg> facets) {

            facets.forEach(facet -> {
                String name = facet.getCommand();
                switch (facet.getType()) {
                    case NUMBER_RANGE:
                        Integer rangeStart = Integer.parseInt(facet.getRangeStart());
                        Integer rangeEnd = Integer.parseInt(facet.getRangeEnd());
                        Integer rangeGap = Integer.parseInt(facet.getRangeGap());
                        this.query.addNumericRangeFacet(name, rangeStart, rangeEnd, rangeGap);
                        break;
                    default:
                        this.query.addFacetField(name);
                        break;
                }
            });

            if (!facets.isEmpty()) {
                // NOTE: other possible; method, minCount, missing, and prefix
                this.query.setFacet(true);
                this.query.setFacetLimit(-1);
                this.query.setFacetMinCount(1);
            }

            return this;
        }

        public SolrQueryBuilder withBoosts(List<BoostArg> boosts) {

            return this;
        }

        public SolrQueryBuilder withHighlight(HighlightArg highlight) {

            for (String field : highlight.getFields()) {
                this.query.addHighlightField(field);
            }

            if (highlight.getFields().length > 0) {
                this.query.setHighlight(true);
                this.query.setHighlightFragsize(0);
                this.query.setHighlightSimplePre(highlight.getPrefix());
                this.query.setHighlightSimplePre(highlight.getPostfix());
            }

            return this;
        }

        public SolrQuery query() {
            return this.query;
        }

    }

    public class FilterQueryBuilder {

        private final FilterArg filter;

        private final boolean skipTag;

        public FilterQueryBuilder(FilterArg filter) {
            this.filter = filter;
            this.skipTag = false;
        }

        public FilterQueryBuilder(FilterArg filter, boolean skipTag) {
            this.filter = filter;
            this.skipTag = skipTag;
        }

        public String build() {
            String field = skipTag ? filter.getField() : filter.getCommand();
            String value = filter.getValue();

            StringBuilder filterQuery = new StringBuilder()
                .append(field)
                .append(QUERY_DELIMETER);

            switch (filter.getOpKey()) {
                case BETWEEN:
                    Matcher rangeMatcher = RANGE_PATTERN.matcher(value);
                    if (rangeMatcher.matches()) {
                        String start = rangeMatcher.group(1);
                        String end = rangeMatcher.group(2);

                        // NOTE: hard coded inclusive start exclusive end
                        // criteria.between(start, end, true, false);
                        filterQuery
                            .append("[")
                            .append(start)
                            .append(" TO ")
                            .append(end)
                            .append("}");

                        // NOTE: if date field, must be ISO format for Solr to recognize
                        // https://lucene.apache.org/solr/7_5_0/solr-core/org/apache/solr/schema/DatePointField.html
                        
                    } else {
                        // criteria.is(value);
                        filterQuery
                            .append("\"")
                            .append(value)
                            .append("\"");
                    }
                    break;
                case ENDS_WITH:
                    // criteria.endsWith(value);
                    filterQuery
                        .append(value)
                        .append("}");
                    break;
                case EQUALS:
                    // criteria.is(value);
                    filterQuery
                        .append("\"")
                        .append(value)
                        .append("\"");
                    break;
                case FUZZY:
                    // NOTE: only supporting single-word terms and default edit distance of 2
                    // criteria.fuzzy(value);
                    filterQuery
                        .append(value)
                        .append("~");
                    break;
                case NOT_EQUALS:
                    // criteria.is(value).not();
                    filterQuery
                        .append("!")
                        .append("\"")
                        .append(value)
                        .append("\"");
                    break;
                case STARTS_WITH:
                    // criteria.startsWith(value);
                    filterQuery
                        .append("{!")
                        .append(value);
                    break;
                case CONTAINS:
                    // criteria.contains(value);
                case EXPRESSION:
                    // criteria.expression(value);
                case RAW:
                    // criteria = new SimpleStringCriteria(String.format("%s:%s", field, value));
                    filterQuery
                        .append(value);
                default:
                    break;
            }

            return filterQuery.toString();
        }

    }

}
