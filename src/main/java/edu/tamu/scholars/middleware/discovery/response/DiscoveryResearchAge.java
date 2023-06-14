package edu.tamu.scholars.middleware.discovery.response;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.tamu.scholars.middleware.discovery.argument.DiscoveryResearchAgeDescriptor;
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

    public void from(Map<String, Integer> facet, List<String[]> facetQueries) {
        for (int j = 0; j < facetQueries.size(); j++) {
            String[] fql = facetQueries.get(j);
            int subtotal = facet.get(fql[0]);

            System.out.println(fql[0] + "::" + fql[1] + ": " + subtotal);

            add(fql[0], fql[1], subtotal);
        }
    }

    public void from(String ageField, SolrDocumentList docs, List<String[]> facetQueries) {

        System.out.println(ageField);

        AtomicInteger total = new AtomicInteger(0);

        facetQueries.stream().forEach(fql -> {

            int subtotal = 0;

            for (SolrDocument solrDoc : docs) {
                long dateFromEpochInSeconds = (long) solrDoc.getFieldValue(ageField);

                int age = DateFormatUtility.ageInYearsFromEpochSecond(dateFromEpochInSeconds);

                boolean inRange = false;

                String part = fql[2];
                int from = Integer.parseInt(fql[3]);
                int to = Integer.parseInt(fql[4]);

                if (part.equals("first")) {
                    inRange = age < to;
                } else if (part.equals("last")) {
                    inRange = age >= from;
                } else {
                    // in between
                    inRange = age > from && age <= to;
                }

                if (inRange) {
                    subtotal++;
                }
            }

            System.out.println(String.join(",", fql) + " = " + subtotal);

            total.addAndGet(subtotal);

            add(fql[0], fql[1], subtotal);
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
