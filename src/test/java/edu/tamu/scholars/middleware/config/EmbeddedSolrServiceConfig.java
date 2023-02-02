package edu.tamu.scholars.middleware.config;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.core.NodeConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("test")
public class EmbeddedSolrServiceConfig {

    @Bean
    public EmbeddedSolrServer solrServer() throws Exception {
        final String solrHome = "target/solr";
        final String configSetHome = "solr";
        final String coreName = "scholars-discovery";

        final File solrHomeDir = new File(solrHome);
        if (solrHomeDir.exists()) {
            FileUtils.deleteDirectory(solrHomeDir);
            solrHomeDir.mkdirs();
        } else {
            solrHomeDir.mkdirs();
        }

        final Path solrHomePath = Paths.get(solrHome).toAbsolutePath();

        final NodeConfig config = new NodeConfig.NodeConfigBuilder("scholasr-discovery", solrHomePath)
                .setConfigSetBaseDirectory(configSetHome)
                .build();

        return new EmbeddedSolrServer(config, coreName);
    }

}
