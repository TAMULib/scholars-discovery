package edu.tamu.scholars.middleware.discovery.response;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

import edu.tamu.scholars.middleware.discovery.argument.DiscoveryResearchAgeDescriptor;
import edu.tamu.scholars.middleware.discovery.argument.DiscoveryResearchAgeDescriptor.LabeledRange;
import edu.tamu.scholars.middleware.discovery.utility.DateUtility;

public class DiscoveryResearchAge {

    private final String label;

    private final String dateField;

    private final Map<String, String> ranges;

    private final List<AgeGroup> groups;

    private double mean;

    private int median;

    public DiscoveryResearchAge(String label, String dateField) {
        this.label = label;
        this.dateField = dateField;
        this.ranges = new HashMap<>();
        this.groups = new ArrayList<>();
    }

    private synchronized void add(Integer index, String range, String label, Integer value) {
        if (!ranges.containsKey(label)) {
            ranges.put(label, range);
            groups.add(new AgeGroup(index, label, value));
        }
    }

    public void from(DiscoveryResearchAgeDescriptor researcherAgeDescriptor, SolrDocumentList results) {
        String dateField = researcherAgeDescriptor.getDateField();
        String ageField = researcherAgeDescriptor.getAgeField();

        List<LabeledRange> labeledRanges = researcherAgeDescriptor.getLabeledRanges();

        AtomicInteger sum = new AtomicInteger(0);
        AtomicInteger total = new AtomicInteger(0);

        labeledRanges.parallelStream().forEach(lr -> {

            int subtotal = 0;

            List<Integer> set = new ArrayList<>();

            for (SolrDocument solrDoc : results) {
                long dateFromEpochInSeconds = (long) solrDoc.getFieldValue(ageField);

                int age = DateUtility.ageInYearsFromEpochSecond(dateFromEpochInSeconds);

                boolean inRange = false;

                if (lr.isFirst) {
                    sum.getAndAdd(age);
                    inRange = age < lr.to;
                } else if (lr.isLast) {
                    inRange = age >= lr.from;
                } else {
                    inRange = age >= lr.from && age < lr.to;
                }

                if (inRange) {
                    if (researcherAgeDescriptor.getAccumulateMultivaluedDate() && solrDoc.containsKey(dateField)) {
                        Collection<Object> docs = solrDoc.getFieldValues(dateField);
                        subtotal += docs.size();

                        set.add(docs.size());
                    } else {
                        subtotal++;
                    }
                }
            }

            total.addAndGet(subtotal);

            Integer value = researcherAgeDescriptor.getAverageOverInterval() && set.size() > 0
                ? subtotal / set.size()
                : subtotal;

            add(lr.index, lr.range, lr.label, value);

        });

        this.mean = results.size() > 0 ? sum.get() / results.size() : 0;
        this.median = results.size() > 0 ? DateUtility.ageInYearsFromEpochSecond((long) results.get(results.size() / 2).getFieldValue(ageField)) : 0;
    }

    public String getLabel() {
        return label;
    }

    public String getDateField() {
        return dateField;
    }

    public Map<String, String> getRanges() {
        return ranges;
    }

    public List<AgeGroup> getGroups() {
        // NOTE: sorting on serialization in response
        return groups.stream().sorted(new Comparator<AgeGroup>() {

            @Override
            public int compare(AgeGroup o1, AgeGroup o2) {
                return o1.getIndex().compareTo(o2.getIndex());
            }
            
        }).collect(Collectors.toList());
    }

    public double getMean() {
        return this.mean;
    }

    public int getMedian() {
        return this.median;
    }

    public static DiscoveryResearchAge create(String label, String dateField) {
        return new DiscoveryResearchAge(label, dateField);
    }

    public class AgeGroup {
        private final Integer index;
        private final String label;
        private final Integer value;
        private AgeGroup(Integer index, String label, Integer value) {
            this.index = index;
            this.label = label;
            this.value = value;
        }
        public Integer getIndex() {
            return index;
        }
        public String getLabel() {
            return label;
        }
        public Integer getValue() {
            return value;
        }
    }

}
