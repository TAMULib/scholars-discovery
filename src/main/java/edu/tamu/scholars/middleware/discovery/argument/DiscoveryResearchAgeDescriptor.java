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
        int diffStart;
        while (i <= bound) {
            diffStart = i + 1;
            if (i == 0) {
                // first

                LabeledRange imf = new LabeledRange();
                imf.label = "Below 1";
                imf.range = "[0 TO 1}";
                imf.from = 0;
                imf.to = 1;
                imf.isFirst = true;
                imf.isLast = i == bound;

                labeledRanges.add(imf);
                prevStart = 1;
            } else if (i >= bound) {
                // last
                diffStart = LocalDate.now().getYear();

                LabeledRange imf = new LabeledRange();
                imf.label = prevStart + " or Above";
                imf.range = String.format("[%s TO %s]", prevStart, diffStart);
                imf.from = prevStart;
                imf.to = diffStart;
                imf.isFirst = false;
                imf.isLast = true;

                labeledRanges.add(imf);
            } else {
                // in between

                LabeledRange imf = new LabeledRange();
                imf.label = prevStart + " to " + (prevStart + getGroupingIntervalInYears() - 1);
                imf.range = String.format("[%s TO %s]", prevStart, diffStart);
                imf.from = prevStart;
                imf.to = diffStart;
                imf.isFirst = false;
                imf.isLast = false;

                labeledRanges.add(imf);
                prevStart = diffStart;
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
