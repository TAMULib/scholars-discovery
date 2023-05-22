package edu.tamu.scholars.middleware.discovery.service;

import static edu.tamu.scholars.middleware.discovery.DiscoveryConstants.COLLECTION;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import jakarta.annotation.PostConstruct;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.solr.client.solrj.request.CollectionAdminRequest;
import org.apache.solr.client.solrj.request.ConfigSetAdminRequest;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.core.io.Resource;
import org.springframework.util.ResourceUtils;

import edu.tamu.scholars.middleware.config.model.IndexConfig;
import edu.tamu.scholars.middleware.discovery.component.Harvester;
import edu.tamu.scholars.middleware.discovery.component.Indexer;
import edu.tamu.scholars.middleware.service.Triplestore;

@Service
public class IndexService {

    private final static Logger logger = LoggerFactory.getLogger(IndexService.class);

    private final static AtomicBoolean indexing = new AtomicBoolean(false);

    public static final List<String> CREATED_FIELDS = new CopyOnWriteArrayList<String>();

    @Autowired
    private IndexConfig indexConfig;

    @Value("classpath:/solr/conf")
    private Resource configset;

    @Autowired
    private List<Harvester> harvesters;

    @Autowired
    private SolrClient solrClient;

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

        if (indexConfig.getRemoveOnStartup()) {
            try {
                removeCollection();
            } catch(Exception e) {
                // implement robust init exception handling
                // trace code back and change here with logging and such
                e.printStackTrace();
            }
        }

        try {
            createCollection();
        } catch(Exception e) {
            // implement robust init exception handling
            // trace code back and change here with logging and such
            e.printStackTrace();
        }

        logger.info("Initializing {} indexers...", indexers.size());

        indexers.forEach(indexer -> {

            logger.info("Initializing {} fields.", indexer.type().getSimpleName());
            indexer.init();
        });

        if (indexConfig.isOnStartup()) {
            threadPoolTaskScheduler.schedule(new Runnable() {

                @Override
                public void run() {
                    index();
                }

            }, new Date(System.currentTimeMillis() + indexConfig.getOnStartupDelay()));
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
                    harvester.harvest()
                        .buffer(indexConfig.getBatchSize())
                        .subscribe(batch -> {

                        indexers.parallelStream()
                            .filter(indexer -> indexer.type().equals(harvester.type()))
                            .forEach(indexer -> indexer.index(batch));

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

    private void removeCollection() throws IOException, SolrServerException {
        try {
            logger.info("Removing collection {}", COLLECTION);
            CollectionAdminRequest.Delete deleteCollectionRequest = CollectionAdminRequest.deleteCollection(COLLECTION);

            deleteCollectionRequest.process(solrClient);
        } catch(SolrServerException e) {
            e.printStackTrace();
        }
    }

    private void createCollection() throws IOException, SolrServerException {

        // maybe separate zip and upload configset and layer in logic

        logger.info("Zipping configset for collection {}", COLLECTION);
        File zipFile = zipConfigset();

        try {
            logger.info("Uploading configset for collection {}", COLLECTION);
            ConfigSetAdminRequest.Upload uploadConfigsetRequest = new ConfigSetAdminRequest.Upload()
                .setConfigSetName(COLLECTION)
                .setCleanup(true)
                .setOverwrite(true)
                .setUploadFile(zipFile, "application/zip");

            uploadConfigsetRequest.process(solrClient);
        } catch(SolrServerException e) {
            e.printStackTrace();
        }

        try {
            logger.info("Creating collection {}", COLLECTION);
            CollectionAdminRequest.Create createCollectionRequest = CollectionAdminRequest.createCollection(COLLECTION, 1, 1);

            createCollectionRequest.process(solrClient);
        } catch(SolrServerException e) {
            e.printStackTrace();
        }
    }

    private File zipConfigset() throws FileNotFoundException, IOException {
        File zipFile = File.createTempFile("configset", ".zip");

        try (
            FileOutputStream fos = new FileOutputStream(zipFile.getAbsolutePath());
            ZipOutputStream zos = new ZipOutputStream(fos);
        ) {
            File solrConfigSet = configset.getFile();
            if (solrConfigSet.isDirectory()) {
                for (File file : solrConfigSet.listFiles()) {
                    if (file.isDirectory()) {
                        zipDirectory(zos, file, file.getName());
                    } else {
                        zipFile(zos, file);
                    }
                }
            }
        }

        return zipFile;
    }

    private void zipDirectory(ZipOutputStream zos, File directory, String dirEntryParent) throws FileNotFoundException, IOException {
        for (File file : directory.listFiles()) {
            
            if (file.isHidden()) {
                throw new RuntimeException("Attempting to access hidden file: " + file.getName());
            }

            String path = dirEntryParent + File.separator + file.getName();
            if (file.isDirectory()) {
                zipDirectory(zos, file, path);
                continue;
            }

            zos.putNextEntry(new ZipEntry(path));

            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
            long bytesRead = 0;
            byte[] bytesIn = new byte[1024];
            int read = 0;
            while ((read = bis.read(bytesIn)) != -1) {
                zos.write(bytesIn, 0, read);
                bytesRead += read;
            }

            zos.closeEntry();
        }
    }

    private void zipFile(ZipOutputStream zos, File file) throws FileNotFoundException, IOException {
        zos.putNextEntry(new ZipEntry(file.getName()));

        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
        long bytesRead = 0;
        byte[] bytesIn = new byte[1024];
        int read = 0;
        while ((read = bis.read(bytesIn)) != -1) {
            zos.write(bytesIn, 0, read);
            bytesRead += read;
        }

        zos.closeEntry();
    }

}
