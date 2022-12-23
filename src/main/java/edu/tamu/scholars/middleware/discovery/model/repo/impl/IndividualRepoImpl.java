package edu.tamu.scholars.middleware.discovery.model.repo.impl;

import static edu.tamu.scholars.middleware.discovery.DiscoveryConstants.*;
import static edu.tamu.scholars.middleware.discovery.DiscoveryConstants.DEFAULT_QUERY;
import static edu.tamu.scholars.middleware.discovery.DiscoveryConstants.ID;
import static edu.tamu.scholars.middleware.discovery.DiscoveryConstants.MOD_TIME;
import static edu.tamu.scholars.middleware.discovery.DiscoveryConstants.REQUEST_PARAM_DELIMETER;
import static edu.tamu.scholars.middleware.discovery.DiscoveryConstants.TYPE;
import static org.springframework.data.solr.core.query.Criteria.WILDCARD;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.solr.common.params.FacetParams.FacetRangeInclude;
import org.apache.solr.common.params.FacetParams.FacetRangeOther;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.mapping.SimpleSolrMappingContext;
import org.springframework.data.solr.core.mapping.SolrDocument;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.FacetOptions;
import org.springframework.data.solr.core.query.FacetOptions.FieldWithFacetParameters;
import org.springframework.data.solr.core.query.FacetOptions.FieldWithNumericRangeParameters;
import org.springframework.data.solr.core.query.HighlightOptions;
import org.springframework.data.solr.core.query.Query.Operator;
import org.springframework.data.solr.core.query.SimpleFilterQuery;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.data.solr.core.query.SimpleStringCriteria;
import org.springframework.data.solr.core.query.result.Cursor;
import org.springframework.data.solr.core.query.result.FacetAndHighlightPage;

import edu.tamu.scholars.middleware.discovery.argument.BoostArg;
import edu.tamu.scholars.middleware.discovery.argument.FacetArg;
import edu.tamu.scholars.middleware.discovery.argument.FilterArg;
import edu.tamu.scholars.middleware.discovery.argument.FilterGroupArg;
import edu.tamu.scholars.middleware.discovery.argument.HighlightArg;
import edu.tamu.scholars.middleware.discovery.argument.QueryArg;
import edu.tamu.scholars.middleware.discovery.model.Individual;
import edu.tamu.scholars.middleware.discovery.model.repo.custom.SolrDocumentRepoCustom;
import edu.tamu.scholars.middleware.discovery.query.CustomSimpleFacetAndHighlightQuery;
import edu.tamu.scholars.middleware.discovery.query.CustomSimpleFacetQuery;
import edu.tamu.scholars.middleware.discovery.query.parser.CustomSimpleFacetAndHighlightQueryParser;
import edu.tamu.scholars.middleware.discovery.query.parser.CustomSimpleFacetQueryParser;
import edu.tamu.scholars.middleware.model.FilterOp;
import io.micrometer.core.instrument.util.StringUtils;

public class IndividualRepoImpl implements SolrDocumentRepoCustom<Individual> {

    private static final Pattern RANGE_PATTERN = Pattern.compile("^\\[(.*?) TO (.*?)\\]$");

    @Value("${spring.data.solr.parser:edismax}")
    private String defType;

    @Value("${spring.data.solr.operator:AND}")
    private Operator defaultOperator;

    @Autowired
    private SolrTemplate solrTemplate;

    @PostConstruct
    public void setup() {
        // https://jira.spring.io/browse/DATASOLR-153
        // https://github.com/spring-projects/spring-data-solr/pull/113
        solrTemplate.registerQueryParser(CustomSimpleFacetQuery.class, new CustomSimpleFacetQueryParser(new SimpleSolrMappingContext()));
        solrTemplate.registerQueryParser(CustomSimpleFacetAndHighlightQuery.class, new CustomSimpleFacetAndHighlightQueryParser(new SimpleSolrMappingContext()));
    }

    @Override
    public long count(String query, List<FilterArg> filters) {
        SimpleQuery simpleQuery = buildSimpleQuery(filters);
        simpleQuery.addCriteria(buildQueryCriteria(query));
        return solrTemplate.count(collection(), simpleQuery, type());
    }

    @Override
    public List<Individual> findByType(String type, List<FilterArg> filters) {
        filters.add(FilterArg.of(TYPE, Optional.of(type), Optional.of(FilterOp.EQUALS.getKey()), Optional.empty()));
        return findAll(filters);
    }

    @Override
    public List<Individual> findMostRecentlyUpdate(Integer limit) {
        return findMostRecentlyUpdate(limit, new ArrayList<FilterArg>());
    }

    @Override
    public List<Individual> findMostRecentlyUpdate(Integer limit, List<FilterArg> filters) {
        SimpleQuery simpleQuery = buildSimpleQuery(filters);
        simpleQuery.addCriteria(buildQueryCriteria(DEFAULT_QUERY));
        simpleQuery.addSort(Sort.by(MOD_TIME).descending());
        simpleQuery.setRows(limit);
        return solrTemplate.query(collection(), simpleQuery, type()).getContent();
    }

    @Override
    public List<Individual> findAll(List<FilterArg> filters) {
        SimpleQuery simpleQuery = buildSimpleQuery(filters);
        simpleQuery.addCriteria(buildQueryCriteria(DEFAULT_QUERY));
        simpleQuery.setRows(Integer.MAX_VALUE);
        return solrTemplate.query(collection(), simpleQuery, type()).getContent();
    }

    @Override
    public List<Individual> findAll(List<FilterArg> filters, Sort sort) {
        SimpleQuery simpleQuery = buildSimpleQuery(filters);
        simpleQuery.addCriteria(buildQueryCriteria(DEFAULT_QUERY));
        simpleQuery.addSort(sort);
        simpleQuery.setRows(Integer.MAX_VALUE);
        return solrTemplate.query(collection(), simpleQuery, type()).getContent();
    }

    @Override
    public Page<Individual> findAll(List<FilterArg> filters, Pageable page) {
        SimpleQuery simpleQuery = buildSimpleQuery(filters);
        simpleQuery.addCriteria(buildQueryCriteria(DEFAULT_QUERY));
        simpleQuery.setPageRequest(page);
        return solrTemplate.queryForPage(collection(), simpleQuery, type());
    }

    @Override
    public FacetAndHighlightPage<Individual> search(QueryArg query, List<FacetArg> facets, List<FilterArg> filters, List<FilterGroupArg> filterGroups, List<BoostArg> boosts, HighlightArg highlight, Pageable page) {
        CustomSimpleFacetAndHighlightQuery advancedQuery = new CustomSimpleFacetAndHighlightQuery();

        // HERE
        System.out.println(String.format("\n\n%s filter groups", filterGroups.size()));
        filterGroups.forEach(fg -> {
            System.out.println(String.format("a: %s", fg.getA()));
            System.out.println(String.format("o: %s", fg.getExpOp()));
            System.out.println(String.format("b: %s", fg.getB()));
        });
        System.out.println("\n\n");

        Criteria criteria = buildBoostedQueryCriteria(query.getExpression(), boosts);

        advancedQuery.addCriteria(criteria);

        // NOTE: solr does not return total number of facet entries, nor afford direction of sort
        FacetOptions facetOptions = new FacetOptions();

        facets.forEach(facet -> {
            String name = facet.getCommand();
            switch (facet.getType()) {
            case NUMBER_RANGE:
                Integer rangeStart = Integer.parseInt(facet.getRangeStart());
                Integer rangeEnd = Integer.parseInt(facet.getRangeEnd());
                Integer rangeGap = Integer.parseInt(facet.getRangeGap());
                facetOptions.addFacetByRange(new FieldWithNumericRangeParameters(name, rangeStart, rangeEnd, rangeGap)
                    .setHardEnd(false)
                    .setOther(FacetRangeOther.BETWEEN)
                    .setInclude(FacetRangeInclude.LOWER));
                break;
            default:
                // NOTE: other possible; method, minCount, missing, and prefix
                facetOptions.addFacetOnField(new FieldWithFacetParameters(name));
                break;
            }
        });

        if (facetOptions.hasFacets()) {
            facetOptions.setFacetLimit(-1);
            advancedQuery.setFacetOptions(facetOptions);
        }

        // fq added are intersection between filter and cached individually
        buildFilterQueries(filters, filterGroups).forEach(filterQuery -> {
            advancedQuery.addFilterQuery(filterQuery);
        });

        advancedQuery.setDefaultOperator(defaultOperator);

        advancedQuery.setDefType(defType);

        advancedQuery.setPageRequest(page);

        if (StringUtils.isNotEmpty(query.getDefaultField())) {
            advancedQuery.setDefaultField(query.getDefaultField());
        }

        if (StringUtils.isNotEmpty(query.getMinimumShouldMatch())) {
            advancedQuery.setMinimumShouldMatch(query.getMinimumShouldMatch());
        }

        if (StringUtils.isNotEmpty(query.getQueryField())) {
            advancedQuery.setQueryField(query.getQueryField());
        }

        if (StringUtils.isNotEmpty(query.getBoostQuery())) {
            advancedQuery.setBoostQuery(query.getBoostQuery());
        }

        if (StringUtils.isNotEmpty(query.getFields())) {
            advancedQuery.addProjectionOnFields(buildFields(query));
        }

        if (ArrayUtils.isNotEmpty(highlight.getFields())) {
            HighlightOptions highlightOptions = new HighlightOptions();
            highlightOptions.setFragsize(0);
            highlightOptions.addField(highlight.getFields());
            if (StringUtils.isNotBlank(highlight.getPrefix())) {
                highlightOptions.setSimplePrefix(highlight.getPrefix());
            }
            if (StringUtils.isNotBlank(highlight.getPostfix())) {
                highlightOptions.setSimplePostfix(highlight.getPostfix());
            }
            advancedQuery.setHighlightOptions(highlightOptions);
        }

        return solrTemplate.queryForFacetAndHighlightPage(collection(), advancedQuery, type());
    }

    @Override
    public Cursor<Individual> stream(QueryArg query, List<FilterArg> filters, List<BoostArg> boosts, Sort sort) {
        SimpleQuery simpleQuery = buildSimpleQuery(filters);

        Criteria criteria = buildBoostedQueryCriteria(query.getExpression(), boosts);

        simpleQuery.addCriteria(criteria);
        simpleQuery.addSort(sort.and(Sort.by(Direction.ASC, ID)));

        if (StringUtils.isNotEmpty(query.getFields())) {
            simpleQuery.addProjectionOnFields(buildFields(query));
        }

        return solrTemplate.queryForCursor(collection(), simpleQuery, type());
    }

    public String collection() {
        return type().getAnnotation(SolrDocument.class).collection();
    }

    private Criteria buildQueryCriteria(String query) {
        return query.equals(DEFAULT_QUERY) ? new Criteria(WILDCARD).expression(WILDCARD) : new SimpleStringCriteria(query);
    }

    private Criteria buildBoostedQueryCriteria(String query, List<BoostArg> boosts) {
        if (query.equals(DEFAULT_QUERY)) {
            return new Criteria(WILDCARD).expression(WILDCARD);
        }
        Criteria criteria = new SimpleStringCriteria(query).connect();
        String expression = String.format(PARENTHESES_TEMPLATE, query);
        boosts.stream().map(boost -> Criteria.where(boost.getField()).expression(expression).boost(boost.getValue())).forEach(boostCriteria -> {
            criteria.or(boostCriteria.connect());
        });
        return criteria;
    }

    private String buildFields(QueryArg query) {
        String fields = String.join(REQUEST_PARAM_DELIMETER, ID, CLASS, query.getFields());
        return String.join(REQUEST_PARAM_DELIMETER, Arrays.stream(fields.split(REQUEST_PARAM_DELIMETER)).collect(Collectors.toSet()));
    }

    private SimpleQuery buildSimpleQuery(List<FilterArg> filters) {
        SimpleQuery simpleQuery = new SimpleQuery();
        // fq added are intersection between filter and cached individually
        buildFilterQueries(filters, new ArrayList<>()).forEach(filterQuery -> {
            simpleQuery.addFilterQuery(filterQuery);
        });
        simpleQuery.setDefaultOperator(defaultOperator);
        simpleQuery.setDefType(defType);
        return simpleQuery;
    }

    private List<SimpleFilterQuery> buildFilterQueries(List<FilterArg> filters, List<FilterGroupArg> filterGroups) {
        final List<SimpleFilterQuery> results = new ArrayList<SimpleFilterQuery>();

        // group filters specified by args
        filterGroups.forEach(filterGroup -> {
            FilterArg foA = null;
            FilterArg foB = null;

            for (int i = filters.size() - 1; i >= 0; --i) {
                FilterArg filter = filters.get(i);

                // remove filter as operand a, continue if operand b not found yet
                if (foA == null && filterGroup.getA().equals(filter.getField())) {
                    foA = filter;
                    filters.remove(i);

                    if (foB == null) {
                        continue;
                    }
                // remove filter as operand b, continue if operand a not found yet
                } else if (foB == null && filterGroup.getB().equals(filter.getField())) {
                    foB = filter;
                    filters.remove(i);

                    if (foA == null) {
                        continue;
                    }
                }

                // if operand a and b filters are found group according to operator and break loop
                if (foA != null && foB != null) {
                    Criteria criteria = buildCriteria(foA);

                    switch (filterGroup.getExpOp()) {
                        case AND:
                            criteria = criteria.and(buildCriteria(foB, true));
                            break;
                        case OR:
                            criteria = criteria.or(buildCriteria(foB, true));
                            break;
                        default:
                            break;
                    }

                    results.add(new SimpleFilterQuery(criteria));
                    break;
                }
            }
        });

        // proceed with default filter grouping behavior

        // group by field (within same facet)
        filters.stream().collect(Collectors.groupingBy(w -> w.getField())).forEach((field, filterList) -> {
            FilterArg ff = filterList.get(0);
            Criteria criteria = buildCriteria(ff);
            // AND filters within a facet
            if (filterList.size() > 1) {
                for (FilterArg nf : filterList.subList(1, filterList.size())) {
                    criteria = criteria.and(buildCriteria(nf, true));
                }
            }
            results.add(new SimpleFilterQuery(criteria));
        });
        return results;
    }

    @Override
    public Class<Individual> type() {
        return Individual.class;
    }

    private Criteria buildCriteria(FilterArg filter) {
        return buildCriteria(filter, false);
    }

    private Criteria buildCriteria(FilterArg filter, Boolean skipTag) {
        String field = skipTag ? filter.getField() : filter.getCommand();
        String value = filter.getValue();
        Criteria criteria = Criteria.where(field);
        switch (filter.getOpKey()) {
        case BETWEEN:
            Matcher rangeMatcher = RANGE_PATTERN.matcher(value);
            if (rangeMatcher.matches()) {
                String start = rangeMatcher.group(1);
                String end = rangeMatcher.group(2);
                // NOTE: if date field, must be ISO format for Solr to recognize
                // https://lucene.apache.org/solr/7_5_0/solr-core/org/apache/solr/schema/DatePointField.html
                criteria.between(start, end, true, false);
            } else {
                criteria.is(value);
            }
            break;
        case CONTAINS:
            criteria.contains(value);
            break;
        case ENDS_WITH:
            criteria.endsWith(value);
            break;
        case EQUALS:
            criteria.is(value);
            break;
        case EXPRESSION:
            criteria.expression(value);
            break;
        case FUZZY:
            // NOTE: more arguments can be used for fuzzy compare, yet unsupported
            criteria.fuzzy(value);
            break;
        case NOT_EQUALS:
            criteria.is(value).not();
            break;
        case STARTS_WITH:
            criteria.startsWith(value);
            break;
        case RAW:
            criteria = new SimpleStringCriteria(String.format("%s:%s", field, value));
        default:
            break;
        }
        return criteria;
    }

}
