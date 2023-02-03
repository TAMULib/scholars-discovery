package edu.tamu.scholars.middleware.config;

import static edu.tamu.scholars.middleware.discovery.DiscoveryConstants.CORE_NAME;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.core.NodeConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("test")
public class SolrTestConfig {

    private final static Path SOLR_HOME = Paths.get("target/solr").toAbsolutePath();
    private final static String NODE_NAME = "discovery";

    @Bean
    public SolrClient solrServer() throws Exception {
        final File solrDir = new File("solr");
        final File solrHome = SOLR_HOME.toFile();

        if (solrHome.exists()) {
            FileUtils.deleteDirectory(solrHome);
        }

        FileUtils.copyDirectory(solrDir, solrHome);

        System.setProperty("solr.install.dir", SOLR_HOME.toFile().getAbsolutePath());

        final Path solrConfigSetsPath = SOLR_HOME.resolve("configsets");

        final NodeConfig config = new NodeConfig.NodeConfigBuilder(NODE_NAME, SOLR_HOME)
                .setConfigSetBaseDirectory(solrConfigSetsPath.toFile().getAbsolutePath())
                .build();

        return new EmbeddedSolrServer(config, CORE_NAME);
    }

}
