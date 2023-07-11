package edu.tamu.scholars.middleware.discovery.response;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.QueryResponse;

import edu.tamu.scholars.middleware.discovery.argument.DiscoveryQuantityDistributionDescriptor;

public class DiscoveryQuantityDistribution {

    private final String label;

    private final String field;

    private final List<Slice> distribution;

    private DiscoveryQuantityDistribution(String label, String field) {
        this.label = label;
        this.field = field;
        this.distribution = new ArrayList<>();
    }

    public String getLabel() {
        return label;
    }

    public String getField() {
        return field;
    }

    public List<Slice> getDistribution() {
        return distribution;
    }

    public void parse(QueryResponse response) {
        response.getFacetField(field)
            .getValues()
            .stream()
            .sorted(new Comparator<Count>() {

            @Override
            public int compare(Count o1, Count o2) {
                return Long.compare(o2.getCount(), o1.getCount());
            }

        }).forEach(value -> distribution.add(new Slice(value.getName(), value.getCount())));
    }

    public static DiscoveryQuantityDistribution from(DiscoveryQuantityDistributionDescriptor quantityDistributionDescriptor) {
        return new DiscoveryQuantityDistribution(quantityDistributionDescriptor.getLabel(), quantityDistributionDescriptor.getField());
    }

    public class Slice {

        private final String label;
        private final Long count;

        public Slice(String label, Long count) {
            this.label = label;
            this.count = count;
        }

        public String getLabel() {
            return label;
        }

        public Long getCount() {
            return count;
        }

    }

}
