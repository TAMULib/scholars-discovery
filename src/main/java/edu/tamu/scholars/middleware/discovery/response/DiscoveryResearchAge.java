package edu.tamu.scholars.middleware.discovery.response;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

import edu.tamu.scholars.middleware.discovery.argument.DiscoveryResearchAgeDescriptor;
import edu.tamu.scholars.middleware.discovery.argument.DiscoveryResearchAgeDescriptor.LabeledRange;
import edu.tamu.scholars.middleware.utility.DateFormatUtility;

public class DiscoveryResearchAge {

    private final String dateField;

    private final Map<String, String> ranges;

    private final List<AgeGroup> groups;

    public DiscoveryResearchAge(String dateField) {
        this.dateField = dateField;
        this.ranges = new HashMap<>();
        this.groups = new ArrayList<>();
    }

    private void add(String range, String label, Integer value) {
        if (!ranges.containsKey(label)) {
            ranges.put(label, range);
            groups.add(new AgeGroup(label, value));
        }
    }

    public void from(DiscoveryResearchAgeDescriptor researcherAgeDescriptor, SolrDocumentList results) {
        String dateField = researcherAgeDescriptor.getDateField();
        String ageField = researcherAgeDescriptor.getAgeField();

        List<LabeledRange> labeledRanges = researcherAgeDescriptor.getLabeledRanges();


        AtomicInteger total = new AtomicInteger(0);

        labeledRanges.stream().forEach(lr -> {

            int subtotal = 0;

            for (SolrDocument solrDoc : results) {
                long dateFromEpochInSeconds = (long) solrDoc.getFieldValue(ageField);

                int age = DateFormatUtility.ageInYearsFromEpochSecond(dateFromEpochInSeconds);

                boolean inRange = false;

                // this in memory range faceting is a nuance
                // please see DiscoveryResearchAgeDescriptor.getLabeledRanges
                if (lr.isFirst) {
                    inRange = age < lr.to;
                } else if (lr.isLast) {
                    inRange = age >= lr.from;
                } else {
                    // in between
                    inRange = age >= lr.from && age < lr.to;
                }

                if (inRange) {
                    if (researcherAgeDescriptor.isMultivaluedField() && solrDoc.containsKey(dateField)) {
                        Collection<Object> docs = solrDoc.getFieldValues(dateField);
                        subtotal += docs.size();
                    } else {
                        subtotal++;
                    }
                }
            }

            // System.out.println(String.join(",", fql) + " = " + subtotal);

            total.addAndGet(subtotal);

            add(lr.range, lr.label, subtotal);

        });

        System.out.println(total);

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
