package edu.tamu.scholars.middleware.discovery.service;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.PostConstruct;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.request.schema.SchemaRequest;
import org.apache.solr.client.solrj.response.SolrPingResponse;
import org.apache.solr.client.solrj.response.schema.SchemaResponse.FieldsResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

import edu.tamu.scholars.middleware.config.model.IndexConfig;
import edu.tamu.scholars.middleware.discovery.DiscoveryConstants;
import edu.tamu.scholars.middleware.discovery.model.repo.IndividualRepo;
import edu.tamu.scholars.middleware.discovery.service.component.AddOnlyAtomicHashSet;
import edu.tamu.scholars.middleware.discovery.service.component.Harvester;
import edu.tamu.scholars.middleware.discovery.service.component.Indexer;
import edu.tamu.scholars.middleware.discovery.service.component.NamedTypedField;
import edu.tamu.scholars.middleware.service.Triplestore;

@Service
public class IndexService {

    private final static Logger logger = LoggerFactory.getLogger(IndexService.class);

    private final static AtomicBoolean schematizing = new AtomicBoolean(false);

    private final static AtomicBoolean indexing = new AtomicBoolean(false);

    public final static Set<String> CREATED_FIELDS = AddOnlyAtomicHashSet.forCreatedFields();

    public final static Map<String, List<NamedTypedField>> SCAFFOLD = new ConcurrentHashMap<>(7);

    @Autowired
    private ThreadPoolTaskScheduler threadPoolTaskScheduler;

    @Autowired
    private IndexConfig index;

    @Autowired
    private SolrClient solrClient;

    @Lazy
    @Autowired
    private IndividualRepo individualRepo;

    @Autowired
    private Triplestore triplestore;

    @Autowired
    private List<Harvester> harvesters;

    @Autowired
    private List<Indexer> indexers;

    public Boolean isSchematizing() {
        return schematizing.get();
    }

    public Boolean isIndexing() {
        return indexing.get();
    }

    public Map<String, List<NamedTypedField>> getScaffold() {
        return SCAFFOLD;
    }

    // TODO: type out the response from Solr or not
    public Map<String, Object> getSchema() {
        Map<String, Object> schema = new HashMap<>();
        Optional<FieldsResponse> fieldsRes = Optional.empty();

        Optional<Object[]> response = Optional.ofNullable(this.ping());

        if (response.isPresent() && (Integer) response.get()[0] == 0) {

            SchemaRequest.Fields fieldsReq = new SchemaRequest.Fields();
            try {
                fieldsRes = Optional.ofNullable(fieldsReq.process(solrClient, index.getName()));
            } catch (SolrServerException | IOException e) {
                logger.error("Unable to get fields from collection", e);
            }

        } else {
            throw new RuntimeException("ping request failed");
        }

        if (fieldsRes.isPresent()) {
            fieldsRes.map(fr -> fr.getFields())
                .get()
                .stream()
                .forEach(field -> {
                    String name = (String) field.get("name");
                    field.remove("name");
                    schema.put(name, field);
                });
            return schema;
        }

        throw new RuntimeException("fields request failed");
    }

    @PostConstruct
    public void startup() {
        indexers.stream().forEach(indexer -> {
            indexer.scaffold();
        });
        if (index.isInitOnStartup()) {
            if (schematizing.compareAndSet(false, true)) {

                Optional<Object[]> response = Optional.ofNullable(this.ping());

                int status = -1;

                if (response.isPresent()) {
                    Object[] obj = response.get();
                    status = (Integer) obj[0];
                }

                // success
                if (status == 0) {
                    logger.info("Initializing index fields for {}", index.getName());

                    Map<String, Object> schema = getSchema();

                    indexers.stream().forEach(indexer -> {
                        logger.info("Initializing fields for {}", indexer.name());
                        indexer.init(schema);
                    });
                } else {
                    logger.warn("Unable to connect to Solr collection {}", index.getName());

                }

                schematizing.set(false);
            }
        }

        if (index.isOnStartup()) {
            threadPoolTaskScheduler.schedule(new Runnable() {

                @Override
                public void run() {
                    index();
                }

            }, new Date(System.currentTimeMillis() + index.getOnStartupDelay()));
        }
    }

    @Scheduled(cron = "${middleware.index.cron}", zone = "${middleware.index.zone}")
    public void index() {
        if (indexing.compareAndSet(false, true)) {
            triplestore.init();
            Instant start = Instant.now();
            logger.info("Indexing...");
            harvesters.parallelStream().forEach(harvester -> {
                logger.info(String.format("Indexing %s documents.", harvester.type().getSimpleName()));
                if (indexers.stream().anyMatch(indexer -> indexer.type().equals(harvester.type()))) {
                    harvester.harvest().buffer(index.getBatchSize()).subscribe(batch -> {
                        indexers.parallelStream().filter(indexer -> indexer.type().equals(harvester.type())).forEach(indexer -> {
                            indexer.index(batch);
                        });
                    });
                } else {
                    logger.warn(String.format("No indexer found for %s documents!", harvester.type().getSimpleName()));
                }
                logger.info(String.format("Indexing %s documents finished.", harvester.type().getSimpleName()));
            });
            indexers.stream().forEach(indexer -> {
                logger.info(String.format("Optimizing %s index.", indexer.type().getSimpleName()));
                indexer.optimize();
            });
            logger.info(String.format("Indexing finished. %s seconds.", Duration.between(start, Instant.now()).toMillis() / 1000.0));
            triplestore.destroy();
            indexing.set(false);
        } else {
            logger.info("Already indexing. Waiting for next schedule.");
        }
    }

    private Object[] ping() {
        Optional<SolrPingResponse> response = Optional.empty();

        // assume nothing and return failed to connect
        Integer status = -1;
        Long count = 0L;
        String message = "";

        try {
            response = Optional.ofNullable(solrClient.ping(index.getName()));
        } catch (SolrServerException | IOException e) {
            logger.error("Unable to connect to Solr", e);
        }

        if (response.isPresent()) {
            status = response.get().getStatus();
            message = (String) response.get().getResponse().get("status");

            if (message.equals("OK")) {
                count = individualRepo.count(DiscoveryConstants.DEFAULT_QUERY, List.of());
            }
        }

        return new Object[] { status, count, message };
    }

}
