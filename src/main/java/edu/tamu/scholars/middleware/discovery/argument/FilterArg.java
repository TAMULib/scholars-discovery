package edu.tamu.scholars.middleware.discovery.argument;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import edu.tamu.scholars.middleware.discovery.utility.DiscoveryUtility;
import edu.tamu.scholars.middleware.model.FilterOp;

public class FilterArg {

    private final String field;

    private final String value;

    private final FilterOp opKey;

    private final String tag;

    FilterArg(String field, String value, FilterOp opKey, String tag) {
        this.field = DiscoveryUtility.findProperty(field);
        this.value = value;
        this.opKey = opKey;
        this.tag = tag;
    }

    public String getValue() {
        return value;
    }

    public FilterOp getOpKey() {
        return opKey;
    }

    public String getTag() {
        return tag;
    }

    public String getField() {
        return field;
    }

    public String getCommand() {
        return StringUtils.isEmpty(tag) ? field : String.format("{!tag=%s}%s", tag, field);
    }

    public static FilterArg of(String field, Optional<String> value, Optional<String> opKey, Optional<String> tag) {
        String valueParam = value.isPresent() ? value.get() : StringUtils.EMPTY;
        FilterOp opKeyParam = opKey.isPresent() ? FilterOp.valueOf(opKey.get()) : FilterOp.EQUALS;
        String tagParam = tag.isPresent() ? tag.get() : StringUtils.EMPTY;
        return new FilterArg(field, valueParam, opKeyParam, tagParam);
    }

}
