package edu.tamu.scholars.middleware.config;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.Http2SolrClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;

import edu.tamu.scholars.middleware.config.model.IndexConfig;
import edu.tamu.scholars.middleware.service.CachingSolrClient;
import edu.tamu.scholars.middleware.service.JwtTokenService;

/**
 * 
 */
@Configuration
@Profile("!test")
public class SolrConfig {

    @Value("${spring.data.solr.host:http://localhost:8983/solr}")
    private String solrHost;

    @Bean
    @Order(2)
    public SolrClient solrClient() {
        return new Http2SolrClient.Builder(solrHost)
                .connectionTimeout(900000)
                .maxConnectionsPerHost(4)
                .build();
    }

    @Bean
    @Order(1)
    @Primary
    public SolrClient solrServer(
        SolrClient solrClient,
        JwtTokenService jwtTokenService,
        IndexConfig index,
        ObjectMapper objectMapper
    ) throws IOException {
        return new CachingSolrClient<SolrClient>(solrClient, jwtTokenService, index, objectMapper);
    }

}
