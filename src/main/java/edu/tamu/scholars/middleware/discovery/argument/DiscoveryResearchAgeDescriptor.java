package edu.tamu.scholars.middleware.discovery.argument;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// TODO: refactor to DiscoveryResearchAgeDescriptorArg and add argument resolver
public class DiscoveryResearchAgeDescriptor {

    private final String dateField;

    private final boolean accumulateMultivaluedDate;

    private final Integer upperLimitInYears;

    private final Integer groupingIntervalInYears;

    private DiscoveryResearchAgeDescriptor(
            String dateField,
            Boolean accumulateMultivaluedDate,
            Integer upperLimitInYears,
            Integer groupingIntervalInYears) {
        super();
        this.dateField = dateField;
        this.accumulateMultivaluedDate = accumulateMultivaluedDate;
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

    public boolean getAccumulateMultivaluedDate() {
        return accumulateMultivaluedDate;
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
                LabeledRange fr = new LabeledRange();
                fr.label = "Below 1";
                fr.range = "[0 TO 1}";
                fr.from = 0;
                fr.to = 1;
                fr.isFirst = true;
                fr.isLast = i == bound;

                labeledRanges.add(fr);
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
                // middle
                LabeledRange mr = new LabeledRange();
                mr.label = prevStart + " to " + (prevStart + getGroupingIntervalInYears() - 1);
                mr.range = String.format("[%s TO %s}", prevStart, nextStart);
                mr.from = prevStart;
                mr.to = nextStart;
                mr.isFirst = false;
                mr.isLast = false;

                labeledRanges.add(mr);
                prevStart = nextStart;
            }
            i += getGroupingIntervalInYears();
        }

        return labeledRanges;
    }

    public static DiscoveryResearchAgeDescriptor of(
            String dateField,
            Boolean accumulateMultivaluedDate,
            Integer upperLimitInYears,
            Integer groupingIntervalInYears) {
        return new DiscoveryResearchAgeDescriptor(
                dateField,
                accumulateMultivaluedDate,
                upperLimitInYears,
                groupingIntervalInYears);
    }

}
