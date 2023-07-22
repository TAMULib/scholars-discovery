package edu.tamu.scholars.middleware.discovery.indicator;

import static edu.tamu.scholars.middleware.discovery.DiscoveryConstants.WILDCARD;

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
import org.springframework.stereotype.Component;

import edu.tamu.scholars.middleware.discovery.model.repo.IndividualRepo;
import edu.tamu.scholars.middleware.discovery.service.IndexService;

@Component("index")
public class IndexHealthIndicator implements HealthIndicator {

    @Autowired
    private SolrClient solrClient;

    @Lazy
    @Autowired
    private IndividualRepo individualRepo;

    @Autowired
    private IndexService indexService;

    @Override
    public Health health() {
        Health.Builder status = Health.down();

        Map<String, Object> details = new HashMap<String, Object>();

        try {
            SolrPingResponse response = solrClient.ping("scholars-discovery");
            String message = (String) response.getResponse().get("status");

            // NOTE: note a REST response status code
            // 0 - successful ping response for given collection
            // details.put("status", response.getStatus());
            details.put("message", message);

            if (response.getStatus() == 0 && message.equals("OK")) {

                long count = individualRepo.count(WILDCARD, List.of());

                details.put("count", count);

                status.up();

                details.put("initializing", indexService.isSchematizing());
                details.put("indexing", indexService.isIndexing());
                details.put("ready", !indexService.isSchematizing() && !indexService.isIndexing());
                details.put("schema", indexService.getSchema());

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
