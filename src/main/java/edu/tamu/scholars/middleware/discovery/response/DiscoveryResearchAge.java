package edu.tamu.scholars.middleware.discovery.response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DiscoveryResearchAge {

    private final String dateField;

    private final Map<String, String> ranges;

    private final List<AgeGroup> groups;

    public DiscoveryResearchAge(String dateField) {
        this.dateField = dateField;
        this.ranges = new HashMap<>();
        this.groups = new ArrayList<>();
    }

    public void add(String range, String label, Integer value) {
        if (!ranges.containsKey(label)) {
            ranges.put(label, range);
            groups.add(new AgeGroup(label, value));
        }
    }

    public String getDateField() {
        return dateField;
    }

    public Map<String, String> getRanges() {
        return ranges;
    }

    public List<AgeGroup> getGroups() {
        return groups;
    }

    public static DiscoveryResearchAge create(String dateField) {
        return new DiscoveryResearchAge(dateField);
    }

    public class AgeGroup {
        private final String label;
        private final Integer value;
        private AgeGroup(String label, Integer value) {
            this.label = label;
            this.value = value;
        }
        public String getLabel() {
            return label;
        }
        public Integer getValue() {
            return value;
        }
    }

}
