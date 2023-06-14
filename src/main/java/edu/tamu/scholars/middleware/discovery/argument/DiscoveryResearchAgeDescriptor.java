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
        Integer groupingIntervalInYears
    ) {
        super();
        this.dateField = dateField;
        this.multivaluedField = multivaluedField;
        this.upperLimitInYears = upperLimitInYears;
        this.groupingIntervalInYears = groupingIntervalInYears;
    }

    public String getDateField() {
        return dateField;
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

    public List<String[]> getFacetQueries() {
        List<String[]> facetQueries = new ArrayList<>();

        String dateField = getDateField();
        int bound = getUpperLimitInYears() + getGroupingIntervalInYears();

        int i = 0;
        int prevStart = i;
        int diffStart;
        while (i <= bound) {
            diffStart = i + 1;
            if (i == 0) {
                // first
                facetQueries.add(new String[] {
                    String.format("%s:{NOW-1YEAR/YEAR TO NOW/YEAR}", dateField),
                    "Below 1",
                    "first",
                    String.valueOf(prevStart),
                    String.valueOf(diffStart)
                });
                prevStart = 1;
            } else if (i >= bound) {
                // last
                diffStart = LocalDate.now().getYear();
                facetQueries.add(new String[] {
                    String.format("%s:[NOW-%sYEAR/YEAR TO NOW-%sYEAR/YEAR]", dateField, diffStart, prevStart),
                    prevStart + " or Above",
                    "last",
                    String.valueOf(prevStart),
                    String.valueOf(diffStart)
                });
            } else {
                // in between
                facetQueries.add(new String[] {
                    String.format("%s:{NOW-%sYEAR/YEAR TO NOW-%sYEAR/YEAR]", dateField, diffStart, prevStart),
                    prevStart + " to " + (prevStart + getGroupingIntervalInYears() - 1),
                    "in between",
                    String.valueOf(prevStart),
                    String.valueOf(diffStart)
                });
                prevStart = diffStart;
            }
            i += getGroupingIntervalInYears();
        }

        return facetQueries;
    }

    public static DiscoveryResearchAgeDescriptor of(
        String dateField,
        Boolean multivaluedField,
        Integer upperLimitInYears,
        Integer groupingIntervalInYears
    ) {
        return new DiscoveryResearchAgeDescriptor(
            dateField,
            multivaluedField,
            upperLimitInYears,
            groupingIntervalInYears
        );
    }

}
