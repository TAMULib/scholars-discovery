package edu.tamu.scholars.middleware.discovery;

import static edu.tamu.scholars.middleware.discovery.DiscoveryConstants.DEFAULT_QUERY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.request.CoreAdminRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.Resource;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.tamu.scholars.middleware.config.SolrTestConfig;
import edu.tamu.scholars.middleware.discovery.component.Indexer;
import edu.tamu.scholars.middleware.discovery.model.AbstractIndexDocument;
import edu.tamu.scholars.middleware.discovery.model.Individual;
import edu.tamu.scholars.middleware.discovery.model.repo.IndividualRepo;

@Import(SolrTestConfig.class)
@TestInstance(Lifecycle.PER_CLASS)
public abstract class AbstractSolrDocumentIntegrationTest<D extends AbstractIndexDocument> {

    @Value("classpath:mock/discovery")
    private Resource mocksDirectoryResource;

    @Value("${middleware.index.name}")
    private String collectionName;

    @Autowired
    protected SolrClient solrClient;

    @Autowired
    private List<Indexer> indexers;

    @Autowired
    protected IndividualRepo repo;

    protected List<D> mockDocuments = new ArrayList<D>();

    protected int numberOfDocuments = 0;

    @BeforeAll
    public void setup() throws SolrServerException, IOException {
        createCore();
        createDocuments();
    }

    @AfterAll
    public void cleanup() throws SolrServerException, IOException {
        deleteDocuments();
        deleteCore();
    }

    private void createCore() throws SolrServerException, IOException {
        CoreAdminRequest.Create createRequest = new CoreAdminRequest.Create();
        createRequest.setCoreName(collectionName);
        createRequest.setConfigSet(collectionName);
        solrClient.request(createRequest);

        indexers.stream()
            .filter(i -> i.type().equals(getType()))
            .forEach(indexer -> {
                indexer.init();
            });
    }

    private void deleteCore() throws SolrServerException, IOException {
        CoreAdminRequest.Unload unloadRequest = new CoreAdminRequest.Unload(true);
        unloadRequest.setCoreName(collectionName);
        solrClient.request(unloadRequest);
    }

    private void createDocuments() throws SolrServerException, IOException {
        assertEquals(0, repo.count(DEFAULT_QUERY, Collections.emptyList()));
        ObjectMapper objectMapper = new ObjectMapper();
        List<File> mockFiles = getMockFiles();
        for (File file : mockFiles) {
            Map<String, Object> content = objectMapper.readValue(file, new TypeReference<Map<String, Object>>() {});

            solrClient.addBean(collectionName, Individual.from(content));

            @SuppressWarnings("unchecked")
            D mockDocument = (D) objectMapper.readValue(file, getType());
            assertNotNull(mockDocument);
            mockDocuments.add(mockDocument);
        }
        assertTrue(mockDocuments.size() > 0, "No mock documents processed");
        solrClient.commit(collectionName);
        numberOfDocuments = (int) repo.count(DEFAULT_QUERY, Collections.emptyList());
        assertEquals(mockFiles.size(), numberOfDocuments, "Indexed documents count not matching mock documents count");
    }

    private void deleteDocuments() throws SolrServerException, IOException {
        solrClient.deleteByQuery(collectionName, DEFAULT_QUERY);
        solrClient.commit(collectionName);
    }

    private List<File> getMockFiles() throws IOException {
        assertTrue(mocksDirectoryResource.exists());
        assertTrue(mocksDirectoryResource.isFile());
        File mocksDirectory = mocksDirectoryResource.getFile();
        assertTrue(mocksDirectory.isDirectory());

        return Files.walk(mocksDirectory.toPath().resolve(getDocPath()), 2)
            .map(path -> path.toFile())
            .filter(file -> file.isFile())
            .collect(Collectors.toList());
    }

    protected String getDocPath() {
        String docPath = getType().getSimpleName().toLowerCase();
        if (docPath.equals("process")) {
            docPath += "es";
        } else {
            docPath += "s";
        }

        return docPath;
    }

    protected abstract Class<?> getType();

}
