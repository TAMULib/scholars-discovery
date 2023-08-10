package edu.tamu.scholars.middleware.discovery.response;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

import edu.tamu.scholars.middleware.discovery.argument.DiscoveryAcademicAgeDescriptor;
import edu.tamu.scholars.middleware.discovery.argument.DiscoveryAcademicAgeDescriptor.LabeledRange;
import edu.tamu.scholars.middleware.discovery.utility.DateUtility;

public class DiscoveryAcademicAge {

    private final String label;

    private final String dateField;

    private final Map<String, String> ranges;

    private final List<AgeGroup> groups;

    private double mean;

    private int median;

    public DiscoveryAcademicAge(String label, String dateField) {
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

    public void from(DiscoveryAcademicAgeDescriptor academicAgeDescriptor, SolrDocumentList results) {
        String dateField = academicAgeDescriptor.getDateField();
        String ageField = academicAgeDescriptor.getAgeField();

        List<LabeledRange> labeledRanges = academicAgeDescriptor.getLabeledRanges();

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
                    if (academicAgeDescriptor.getAccumulateMultivaluedDate() && solrDoc.containsKey(dateField)) {
                        Collection<Object> docs = solrDoc.getFieldValues(dateField);
                        subtotal += docs.size();

                        set.add(docs.size());
                    } else {
                        subtotal++;
                    }
                }
            }

            total.addAndGet(subtotal);

            Integer value = academicAgeDescriptor.getAverageOverInterval() && set.size() > 0
                ? subtotal / set.size()
                : subtotal;

            add(lr.index, lr.range, lr.label, value);

        });

        Collections.sort(groups, new AgeGroupComparator());

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
        return groups;
    }

    public double getMean() {
        return this.mean;
    }

    public int getMedian() {
        return this.median;
    }

    public static DiscoveryAcademicAge create(String label, String dateField) {
        return new DiscoveryAcademicAge(label, dateField);
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

    private class AgeGroupComparator implements Comparator<AgeGroup> {

        @Override
        public int compare(AgeGroup o1, AgeGroup o2) {
            return o1.getIndex().compareTo(o2.getIndex());
        }

    }

}
