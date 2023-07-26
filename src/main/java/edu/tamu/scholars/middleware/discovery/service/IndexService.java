package edu.tamu.scholars.middleware.discovery.service;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

import edu.tamu.scholars.middleware.config.model.IndexConfig;
import edu.tamu.scholars.middleware.discovery.service.component.AddOnlyAtomicHashSet;
import edu.tamu.scholars.middleware.discovery.service.component.Harvester;
import edu.tamu.scholars.middleware.discovery.service.component.Indexer;
import edu.tamu.scholars.middleware.service.Triplestore;

@Service
public class IndexService {

    private final static Logger logger = LoggerFactory.getLogger(IndexService.class);

    private final static AtomicBoolean schematizing = new AtomicBoolean(false);

    private final static AtomicBoolean indexing = new AtomicBoolean(false);

    public final static Set<String> CREATED_FIELDS = AddOnlyAtomicHashSet.forCreatedFields();

    public final static Map<String, List<String>> SCHEMA = new ConcurrentHashMap<>(7);

    @Autowired
    private IndexConfig index;

    @Autowired
    private List<Harvester> harvesters;

    @Autowired
    private List<Indexer> indexers;

    @Autowired
    private Triplestore triplestore;

    @Autowired
    private ThreadPoolTaskScheduler threadPoolTaskScheduler;

    public Boolean isSchematizing() {
        return schematizing.get();
    }

    public Boolean isIndexing() {
        return indexing.get();
    }

    public Map<String, List<String>> getSchema() {
        return SCHEMA;
    }

    @PostConstruct
    public void startup() {
        if (index.isInitOnStartup()) {
            schematize();
        } else {
            logger.info("Scaffolding index fields...");
            indexers.stream().forEach(indexer -> {
                logger.info(String.format("Scaffolding %s fields.", indexer.name()));
                indexer.scaffold();
            });
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

    public void schematize() {
        if (schematizing.compareAndSet(false, true)) {
            logger.info("Initializing index fields...");
            indexers.stream().forEach(indexer -> {
                logger.info(String.format("Initializing %s fields.", indexer.name()));
                indexer.init();
            });
            schematizing.set(false);
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

}
