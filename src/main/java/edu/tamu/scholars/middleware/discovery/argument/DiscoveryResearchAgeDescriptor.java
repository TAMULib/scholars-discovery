package edu.tamu.scholars.middleware.discovery.argument;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// TODO: refactor to DiscoveryResearchAgeDescriptorArg and add argument resolver
public class DiscoveryResearchAgeDescriptor {

    private final String dateField;

    private final boolean multivaluedField;

    private final Integer upperLimitInYears;

    private final Integer groupingIntervalInYears;

    private DiscoveryResearchAgeDescriptor(
            String dateField,
            Boolean multivaluedField,
            Integer upperLimitInYears,
            Integer groupingIntervalInYears) {
        super();
        this.dateField = dateField;
        this.multivaluedField = multivaluedField;
        this.upperLimitInYears = upperLimitInYears;
        this.groupingIntervalInYears = groupingIntervalInYears;
    }

    public String getDateField() {
        return dateField;
    }

    public String getAgeField() {
        // solr field function
        return String.format("field(%s,min)", dateField);
    }

    public boolean isMultivaluedField() {
        return multivaluedField;
    }

    public Integer getUpperLimitInYears() {
        return upperLimitInYears;
    }

    public Integer getGroupingIntervalInYears() {
        return groupingIntervalInYears;
    }

    public class LabeledRange {
        public String label;
        public int from;
        public int to;
        public String range;
        public boolean isFirst;
        public boolean isLast;
    }

    public List<LabeledRange> getLabeledRanges() {
        List<LabeledRange> labeledRanges = new ArrayList<>();

        int bound = getUpperLimitInYears() + getGroupingIntervalInYears();

        int i = 0;
        int prevStart = i;
        int nextStart;
        while (i <= bound) {
            nextStart = i + 1;
            if (i == 0) {
                // first
                LabeledRange lr = new LabeledRange();
                lr.label = "Below 1";
                lr.range = "[0 TO 1}";
                lr.from = 0;
                lr.to = 1;
                lr.isFirst = true;
                lr.isLast = i == bound;

                labeledRanges.add(lr);
                prevStart = 1;
            } else if (i >= bound) {
                // last
                nextStart = LocalDate.now().getYear();

                LabeledRange lr = new LabeledRange();
                lr.label = prevStart + " or Above";
                lr.range = String.format("[%s TO %s]", prevStart, nextStart);
                lr.from = prevStart;
                lr.to = nextStart;
                lr.isFirst = false;
                lr.isLast = true;

                labeledRanges.add(lr);
            } else {
                // in between
                LabeledRange lr = new LabeledRange();
                lr.label = prevStart + " to " + (prevStart + getGroupingIntervalInYears() - 1);
                lr.range = String.format("[%s TO %s}", prevStart, nextStart);
                lr.from = prevStart;
                lr.to = nextStart;
                lr.isFirst = false;
                lr.isLast = false;

                labeledRanges.add(lr);
                prevStart = nextStart;
            }
            i += getGroupingIntervalInYears();
        }

        return labeledRanges;
    }

    public static DiscoveryResearchAgeDescriptor of(
            String dateField,
            Boolean multivaluedField,
            Integer upperLimitInYears,
            Integer groupingIntervalInYears) {
        return new DiscoveryResearchAgeDescriptor(
                dateField,
                multivaluedField,
                upperLimitInYears,
                groupingIntervalInYears);
    }

}
