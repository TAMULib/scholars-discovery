package edu.tamu.scholars.middleware.config;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.CloudHttp2SolrClient;
import org.apache.solr.client.solrj.impl.Http2SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

/**
 * Solr clients configuration for connecting to Apache Solr.
 */
@Configuration
@Profile("!test")
public class SolrConfig {

    @Value("${solr.host:http://localhost:8983/solr}")
    private String solrHost;

    @Value("${solr.default-collection:scholars-discovery}")
    private String defaultCollection;

    @Value("${solr.connection-timeout:15000}")
    private int connectionTimeout;

    @Value("${solr.request-timeout:120000}")
    private int requestTimeout;

    @Value("${solr.idle-timeout:120000}")
    private int idleTimeout;

    @Value("${solr.max-connections-per-host:8}")
    private int maxConnectionsPerHost;

    @Value("${solr.follow-redirects:true}")
    private boolean followRedirects;

    @Value("${solr.cloud.zk-hosts:localhost:2181,localhost:2182,localhost:2183}")
    private List<String> zkHosts;

    @Value("${solr.cloud.zk-chroot:/}")
    private String zkChroot;

    @Value("${solr.cloud.collection-cache-ttl:60000}")
    private int collectionCacheTtl;

    @Value("${solr.cloud.parallel-updates:true}")
    private boolean parallelUpdates;

    @Value("${solr.cloud.parallel-cache-refreshes:8}")
    private int parallelCacheRefreshes;

    @Value("${solr.cloud.retry-expiry-time:30000}")
    private int retryExpiryTime;

    @Value("${solr.cloud.zk-client-timeout:15000}")
    private int zkClientTimeout;

    @Value("${solr.cloud.zk-connection-timeout:10000}")
    private int zkConnectTimeout;

    /**
     * 
     * @return
     * @deprecated
     */
    @Deprecated(since = "8/16/2025")
    @Bean("httpSolrClient")
    @ConditionalOnProperty(value = "solr.client", havingValue = "http1", matchIfMissing = false)
    SolrClient httpSolrClient() {
        return new HttpSolrClient.Builder(solrHost)
            .withDefaultCollection(defaultCollection)
            .withConnectionTimeout(connectionTimeout, TimeUnit.MILLISECONDS)
            .withSocketTimeout(requestTimeout, TimeUnit.MILLISECONDS)
            .withFollowRedirects(followRedirects)
            .build();
    }

    /**
     * 
     * @return
     */
    @Bean("http2SolrClient")
    @ConditionalOnProperty(value = "solr.client", havingValue = "http2", matchIfMissing = true)
    SolrClient http2SolrClient() {
        return new Http2SolrClient.Builder(solrHost)
            .withDefaultCollection(defaultCollection)
            .withConnectionTimeout(connectionTimeout, TimeUnit.MILLISECONDS)
            .withRequestTimeout(requestTimeout, TimeUnit.MILLISECONDS)
            .withIdleTimeout(idleTimeout, TimeUnit.MILLISECONDS)
            .withMaxConnectionsPerHost(maxConnectionsPerHost)
            .withFollowRedirects(followRedirects)
            .build();
    }

    /**
     * 
     * @return
     */
    @Primary
    @Bean("cloudHttp2SolrClient")
    @ConditionalOnProperty(value = "solr.client", havingValue = "cloud", matchIfMissing = false)
    SolrClient cloudHttp2SolrClient() {
        return new CloudHttp2SolrClient.Builder(zkHosts,Optional.of(zkChroot))
            .withCollectionCacheTtl(collectionCacheTtl, TimeUnit.MILLISECONDS)
            .withDefaultCollection(defaultCollection)
            .withParallelUpdates(parallelUpdates)
            .withParallelCacheRefreshes(parallelCacheRefreshes)
            .withRetryExpiryTime(retryExpiryTime, TimeUnit.MILLISECONDS)
            .withZkClientTimeout(zkClientTimeout, TimeUnit.MILLISECONDS)
            .withZkConnectTimeout(zkConnectTimeout, TimeUnit.MILLISECONDS)
            .build();
    }

}
