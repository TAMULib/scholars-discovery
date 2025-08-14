package edu.tamu.scholars.middleware.discovery.service;

import jakarta.annotation.PostConstruct;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

import edu.tamu.scholars.middleware.config.model.IndexConfig;
import edu.tamu.scholars.middleware.discovery.component.Harvester;
import edu.tamu.scholars.middleware.discovery.component.Indexer;
import edu.tamu.scholars.middleware.service.Triplestore;

/**
 * 
 */
@Service
public class IndexService {

    private static final Logger logger = LoggerFactory.getLogger(IndexService.class);

    private static final AtomicBoolean indexing = new AtomicBoolean(false);

    public static final List<String> CREATED_FIELDS = new CopyOnWriteArrayList<String>();

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

    public Boolean isIndexing() {
        return indexing.get();
    }

    @PostConstruct
    public void startup() {
        if (index.isSchematize()) {
            logger.info("Initializing index fields...");
            indexers.stream().forEach(indexer -> {
                logger.info("Initializing {} fields.", indexer.type().getSimpleName());
                indexer.init();
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

    @Scheduled(cron = "${middleware.index.cron}", zone = "${middleware.index.zone}")
    public void index() {
        if (indexing.compareAndSet(false, true)) {
            triplestore.init();
            final Instant start = Instant.now();
            logger.info("Indexing...");
            harvesters.parallelStream().forEach(harvester -> {
                logger.info("Indexing {} documents.", harvester.type().getSimpleName());
                if (indexers.stream().anyMatch(indexer -> indexer.type().equals(harvester.type()))) {
                    harvester.harvest().buffer(index.getBatchSize()).subscribe(batch -> {
                        indexers.parallelStream()
                            .filter(indexer -> indexer.type().equals(harvester.type()))
                            .forEach(indexer -> {
                                indexer.index(batch);
                            });
                    });
                } else {
                    logger.warn("No indexer found for {} documents!", harvester.type().getSimpleName());
                }
                logger.info("Indexing {} documents finished.", harvester.type().getSimpleName());
            });
            indexers.stream().forEach(indexer -> {
                logger.info("Optimizing {} index.", indexer.type().getSimpleName());
                indexer.optimize();
            });
            logger.info("Indexing finished. {} seconds.", Duration.between(start, Instant.now()).toMillis() / 1000.0);
            triplestore.destroy();
            indexing.set(false);
        } else {
            logger.info("Already indexing. Waiting for next schedule.");
        }
    }

}
