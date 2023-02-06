package edu.tamu.scholars.middleware.config;

import static edu.tamu.scholars.middleware.discovery.DiscoveryConstants.CORE_NAME;

import java.io.File;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.Http2SolrClient;
import org.apache.solr.client.solrj.request.CoreAdminRequest;
import org.junit.Rule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.junit.jupiter.Testcontainers;

@Configuration
@Profile("test")
@Testcontainers
public class SolrTestConfig {

    @Rule
    public GenericContainer solrContainer = new GenericContainer(
        new ImageFromDockerfile()
            .withFileFromFile("configsets/scholars-discovery/conf", new File("solr/configsets/scholars-discovery/conf"))
            .withFileFromFile("setup.sh", new File("solr/setup.sh"))
            .withFileFromFile("Dockerfile", new File("solr/Dockerfile")))
                .withExposedPorts(8983)
                .waitingFor(Wait.forHttp("/solr/scholars-discovery/select")
                    .forStatusCode(200));

    @Bean
    public SolrClient solrServer() throws Exception {
        solrContainer.start();

        return new Http2SolrClient.Builder(
            String.format("http://%s:%s/solr", solrContainer.getHost(), solrContainer.getMappedPort(8983))
        ).build();
    }

}
