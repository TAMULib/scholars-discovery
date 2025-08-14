package edu.tamu.scholars.middleware.discovery.utility;

import jakarta.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import edu.tamu.scholars.middleware.discovery.argument.BoostArg;
import edu.tamu.scholars.middleware.discovery.argument.FacetArg;
import edu.tamu.scholars.middleware.discovery.argument.FilterArg;
import edu.tamu.scholars.middleware.discovery.argument.HighlightArg;
import edu.tamu.scholars.middleware.discovery.argument.QueryArg;

/**
 * 
 */
public class ArgumentUtility {

    private static final String FACET_QUERY_PARAM_KEY = "facets";
    private static final String FILTER_QUERY_PARAM_KEY = "filters";
    private static final String BOOST_QUERY_PARAM_KEY = "boost";

    private static final String QUERY_EXPRESSION_QUERY_PARAM_KEY = "q";
    private static final String DEFAULT_FIELD_QUERY_PARAM_KEY = "df";
    private static final String MINIMUM_SHOULD_MATCH_QUERY_PARAM_KEY = "mm";
    private static final String QUERY_FIELD_QUERY_PARAM_KEY = "qf";
    private static final String BOOST_QUERY_QUERY_PARAM_KEY = "bq";
    private static final String FIELDS_QUERY_PARAM_KEY = "fl";

    private static final String HIGHLIGHT_FIELDS_QUERY_PARAM_KEY = "hl";
    private static final String HIGHLIGHT_PRE_QUERY_PARAM_KEY = "hl.prefix";
    private static final String HIGHLIGHT_POST_QUERY_PARAM_KEY = "hl.postfix";

    private static final String FACET_SORT_FORMAT = "%s.sort";
    private static final String FACET_PAGE_SIZE_FORMAT = "%s.pageSize";
    private static final String FACET_PAGE_NUMBER_FORMAT = "%s.pageNumber";
    private static final String FACET_TYPE_FORMAT = "%s.type";
    private static final String FACET_EXCLUDE_TAG_FORMAT = "%s.exclusionTag";
    private static final String FACET_RANGE_START_TAG_FORMAT = "%s.rangeStart";
    private static final String FACET_RANGE_END_TAG_FORMAT = "%s.rangeEnd";
    private static final String FACET_RANGE_GAP_TAG_FORMAT = "%s.rangeGap";

    private static final String FILTER_VALUE_DELIMITER = ";;";
    private static final String FILTER_VALUE_FORMAT = "%s.filter";
    private static final String FILTER_OPKEY_FORMAT = "%s.opKey";
    private static final String FILTER_TAG_FORMAT = "%s.tag";

    private ArgumentUtility() {

    }

    public static List<FacetArg> getFacetArguments(HttpServletRequest request) {
        List<String> parameterNames = Collections.list(request.getParameterNames());
        List<String> fields = parameterNames.stream()
            .filter(paramName -> paramName.equals(FACET_QUERY_PARAM_KEY))
            .map(request::getParameterValues)
            .map(Arrays::asList)
            .flatMap(list -> list.stream())
            .map(s -> s.split(","))
            .map(Arrays::asList)
            .flatMap(list -> list.stream())
            .collect(Collectors.toList());
        return fields.stream().map(field -> {
            final String sortFacet = String.format(FACET_SORT_FORMAT, field);
            final String pageSizeFacet = String.format(FACET_PAGE_SIZE_FORMAT, field);
            final String pageNumberFacet = String.format(FACET_PAGE_NUMBER_FORMAT, field);
            final String typeFacet = String.format(FACET_TYPE_FORMAT, field);
            final String excludeTagFacet = String.format(FACET_EXCLUDE_TAG_FORMAT, field);
            final String rangeStartTagFacet = String.format(FACET_RANGE_START_TAG_FORMAT, field);
            final String rangeEndTagFacet = String.format(FACET_RANGE_END_TAG_FORMAT, field);
            final String rangeGapTagFacet = String.format(FACET_RANGE_GAP_TAG_FORMAT, field);
            Optional<String> sort = Optional.empty();
            Optional<String> pageSize = Optional.empty();
            Optional<String> pageNumber = Optional.empty();
            Optional<String> type = Optional.empty();
            Optional<String> exclusionTag = Optional.empty();
            Optional<String> rangeStart = Optional.empty();
            Optional<String> rangeEnd = Optional.empty();
            Optional<String> rangeGap = Optional.empty();
            for (String paramName : parameterNames) {
                String[] parameterValues = request.getParameterValues(paramName);
                if (Objects.nonNull(parameterValues)) {
                    Optional<String> value = Optional.of(parameterValues[0]);
                    if (paramName.equals(sortFacet)) {
                        sort = value;
                    } else if (paramName.equals(pageSizeFacet)) {
                        pageSize = value;
                    } else if (paramName.equals(pageNumberFacet)) {
                        pageNumber = value;
                    } else if (paramName.equals(typeFacet)) {
                        type = value;
                    } else if (paramName.equals(excludeTagFacet)) {
                        exclusionTag = value;
                    } else if (paramName.equals(rangeStartTagFacet)) {
                        rangeStart = value;
                    } else if (paramName.equals(rangeEndTagFacet)) {
                        rangeEnd = value;
                    } else if (paramName.equals(rangeGapTagFacet)) {
                        rangeGap = value;
                    }
                }
            }
            return FacetArg.of(field, sort, pageSize, pageNumber, type, exclusionTag, rangeStart, rangeEnd, rangeGap);
        }).collect(Collectors.toList());
    }

    public static List<FilterArg> getFilterArguments(HttpServletRequest request) {
        List<String> parameterNames = Collections.list(request.getParameterNames());
        List<String> fields = parameterNames.stream()
            .filter(paramName -> paramName.equals(FILTER_QUERY_PARAM_KEY))
            .map(request::getParameterValues)
            .map(Arrays::asList)
            .flatMap(list -> list.stream())
            .map(s -> s.split(","))
            .map(Arrays::asList)
            .flatMap(list -> list.stream())
            .collect(Collectors.toList());
        List<FilterArg> filters = new ArrayList<FilterArg>();
        fields.stream().forEach(field -> {
            final String valueFilter = String.format(FILTER_VALUE_FORMAT, field);
            final String opKeyFilter = String.format(FILTER_OPKEY_FORMAT, field);
            final String tagFilter = String.format(FILTER_TAG_FORMAT, field);
            String values = StringUtils.EMPTY;
            Optional<String> opKey = Optional.empty();
            Optional<String> tag = Optional.empty();
            for (String paramName : parameterNames) {
                String[] parameterValues = request.getParameterValues(paramName);
                if (Objects.nonNull(parameterValues)) {
                    if (paramName.equals(valueFilter)) {
                        values = parameterValues[0];
                    } else if (paramName.equals(opKeyFilter)) {
                        opKey = Optional.ofNullable(parameterValues[0]);
                    } else if (paramName.equals(tagFilter)) {
                        tag = Optional.ofNullable(parameterValues[0]);
                    } 
                }
            }
            for (String value : values.split(FILTER_VALUE_DELIMITER)) {
                filters.add(FilterArg.of(field, Optional.of(value), opKey, tag));
            }
        });
        return filters;
    }

    public static List<BoostArg> getBoostArguments(HttpServletRequest request) {
        return Collections.list(request.getParameterNames()).stream()
            .filter(paramName -> paramName.equals(BOOST_QUERY_PARAM_KEY))
            .map(request::getParameterValues)
            .map(Arrays::asList) 
            .flatMap(list -> list.stream())
            .map(BoostArg::of)
            .collect(Collectors.toList());
    }

    public static HighlightArg getHightlightArgument(HttpServletRequest request) {
        String fields = request.getParameter(HIGHLIGHT_FIELDS_QUERY_PARAM_KEY);
        Optional<String> prefix = Optional.ofNullable(request.getParameter(HIGHLIGHT_PRE_QUERY_PARAM_KEY));
        Optional<String> postfix = Optional.ofNullable(request.getParameter(HIGHLIGHT_POST_QUERY_PARAM_KEY));
        return HighlightArg.of(StringUtils.isNotEmpty(fields) ? fields.split(",") : new String[] {}, prefix, postfix);
    }

    public static QueryArg getQueryArgument(HttpServletRequest request) {
        Optional<String> expression = Optional.ofNullable(request.getParameter(QUERY_EXPRESSION_QUERY_PARAM_KEY));
        Optional<String> defaultField = Optional.ofNullable(request.getParameter(DEFAULT_FIELD_QUERY_PARAM_KEY));
        Optional<String> minimumShouldMatch = Optional.ofNullable(
            request.getParameter(MINIMUM_SHOULD_MATCH_QUERY_PARAM_KEY)
        );
        Optional<String> queryField = Optional.ofNullable(request.getParameter(QUERY_FIELD_QUERY_PARAM_KEY));
        Optional<String> boostQuery = Optional.ofNullable(request.getParameter(BOOST_QUERY_QUERY_PARAM_KEY));
        Optional<String> fields = Optional.ofNullable(request.getParameter(FIELDS_QUERY_PARAM_KEY));
        return QueryArg.of(expression, defaultField, minimumShouldMatch, queryField, boostQuery, fields);
    }

}
