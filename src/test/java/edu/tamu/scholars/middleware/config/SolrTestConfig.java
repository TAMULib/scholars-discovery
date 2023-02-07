package edu.tamu.scholars.middleware.config;

import static edu.tamu.scholars.middleware.discovery.DiscoveryConstants.CORE_NAME;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.core.CoreContainer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class SolrTestConfig {

    private final static Path SOLR_HOME = Paths.get("test-solr").toAbsolutePath();

    private CoreContainer cores = null;

    @Bean
    public SolrClient solrServer() throws Exception {
        final File solrDir = new File("solr");
        final File solrHome = SOLR_HOME.toFile();

        if (solrHome.exists()) {
            FileUtils.deleteDirectory(solrHome);
        }

        FileUtils.copyDirectory(solrDir, solrHome);

        System.setProperty("solr.solr.home", SOLR_HOME.toString());
        System.setProperty("solr.install.dir", SOLR_HOME.toString());

        cores = CoreContainer.createAndLoad(SOLR_HOME);

        return new EmbeddedSolrServer(cores, CORE_NAME);
    }

}
