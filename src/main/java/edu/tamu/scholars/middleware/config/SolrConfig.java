package edu.tamu.scholars.middleware.config;

import java.util.concurrent.TimeUnit;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Solr configuration with fallback options for connecting to Apache Solr server.
 * Provides both Http2SolrClient (preferred) and HttpSolrClient (fallback) configurations.
 */
@Configuration
@Profile("!test")
public class SolrConfig {

    @Value("${solr.host:http://localhost:8983/solr}")
    private String solrHost;

    @Value("${solr.connection.timeout:15000}")
    private int connectionTimeout;

    @Value("${solr.request.timeout:120000}")
    private int requestTimeout;

    @Bean
    SolrClient createHttpSolrClient() {
        return new HttpSolrClient.Builder(solrHost)
            .withConnectionTimeout(connectionTimeout, TimeUnit.MILLISECONDS)
            .withSocketTimeout(requestTimeout, TimeUnit.MILLISECONDS)
            .withFollowRedirects(true)
            .build();
    }

}
