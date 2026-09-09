package edu.tamu.scholars.middleware.discovery.argument;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import edu.tamu.scholars.middleware.discovery.utility.DiscoveryUtility;
import edu.tamu.scholars.middleware.model.OpKey;

/**
 * 
 */
public class FilterArg {

    private final String field;

    private final String value;

    private final OpKey opKey;

    private final String tag;

    private final String startYear;

    private final String endYear;

    FilterArg(String field, String value, OpKey opKey, String tag, String startYear, String endYear) {
        this.field = DiscoveryUtility.findProperty(field);
        this.value = value;
        this.opKey = opKey;
        this.tag = tag;
        this.startYear = startYear;
        this.endYear = endYear;
    }

    public String getValue() {
        return value;
    }

    public OpKey getOpKey() {
        return opKey;
    }

    public String getTag() {
        return tag;
    }

    public String getField() {
        return field;
    }

    public String getStartYear() {
        return startYear;
    }

    public String getEndYear() {
        return endYear;
    }

    public String getCommand() {
        return StringUtils.isEmpty(tag) ? field : String.format("{!tag=%s}%s", tag, field);
    }

    public static FilterArg of(String field, Optional<String> value, Optional<String> opKey, Optional<String> tag, Optional<String> startYear, Optional<String> endYear) {
        String valueParam = value.isPresent() ? value.get() : StringUtils.EMPTY;
        OpKey opKeyParam = opKey.isPresent() ? OpKey.valueOf(opKey.get()) : OpKey.EQUALS;
        String tagParam = tag.isPresent() ? tag.get() : StringUtils.EMPTY;
        String startYearParam = startYear.isPresent() ? startYear.get() : StringUtils.EMPTY;
        String endYearParam = endYear.isPresent() ? endYear.get() : StringUtils.EMPTY;
        return new FilterArg(field, valueParam, opKeyParam, tagParam, startYearParam, endYearParam);
    }

    @Override
    public String toString() {
        return "FilterArg [field=" + field + ", value=" + value + ", opKey=" + opKey + ", tag=" + tag + ", startYear=" + startYear + ", endYear=" + endYear + "]";
    }

}
