package edu.tamu.scholars.middleware.discovery.indicator;

import static edu.tamu.scholars.middleware.discovery.DiscoveryConstants.DEFAULT_QUERY;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.SolrPingResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import edu.tamu.scholars.middleware.config.model.IndexConfig;
import edu.tamu.scholars.middleware.discovery.model.repo.IndividualRepo;

@Component("index")
@Profile("!test")
public class IndexHealthIndicator implements HealthIndicator {

    @Autowired
    private SolrClient solrClient;

    @Lazy
    @Autowired
    private IndividualRepo individualRepo;

    @Autowired
    private IndexConfig index;

    @Override
    public Health health() {
        Health.Builder status = Health.down();

        Map<String, Object> details = new HashMap<String, Object>();

        try {
            SolrPingResponse response = solrClient.ping(index.getName());

            details.put("name", index.getName());

            String message = (String) response.getResponse().get("status");

            // NOTE: not a REST response status code
            // 0 - successful ping response for given collection
            // details.put("status", response.getStatus());
            details.put("message", message);

            if (response.getStatus() == 0 && message.equals("OK")) {

                long count = individualRepo.count(DEFAULT_QUERY, List.of());

                details.put("count", count);

                status.up();

            } else {
                details.put("response", response.getResponse().asMap(3));
            }
        } catch (SolrServerException | IOException e) {
            e.printStackTrace();
        }

        return status.withDetails(details)
            .build();
    }

}
