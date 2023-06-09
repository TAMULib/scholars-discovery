package edu.tamu.scholars.middleware.discovery.argument;

// TODO: refactor to DiscoveryResearchAgeDescriptorArg and add argument resolver
public class DiscoveryResearchAgeDescriptor {

    private final String dateField;

    private final Integer upperLimitInYears;

    private final Integer groupingIntervalInYears;

    private DiscoveryResearchAgeDescriptor(String dateField, Integer upperLimitInYears, Integer groupingIntervalInYears) {
        super();
        this.dateField = dateField;
        this.upperLimitInYears = upperLimitInYears;
        this.groupingIntervalInYears = groupingIntervalInYears;
    }

    public String getDateField() {
        return dateField;
    }

    public Integer getUpperLimitInYears() {
        return upperLimitInYears;
    }

    public Integer getGroupingIntervalInYears() {
        return groupingIntervalInYears;
    }

    public static DiscoveryResearchAgeDescriptor of(String dateField, Integer upperLimitInYears, Integer groupingIntervalInYears) {
        return new DiscoveryResearchAgeDescriptor(dateField, upperLimitInYears, groupingIntervalInYears);
    }

}
